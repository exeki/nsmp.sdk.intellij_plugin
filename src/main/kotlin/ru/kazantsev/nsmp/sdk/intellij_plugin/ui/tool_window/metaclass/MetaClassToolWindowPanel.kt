package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.storage.MetaClassStorageService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.AbstractBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.MetaClassSyncBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.topics.BackgroundTaskTopic
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.components.MetaClassAttributesTableCopySupport
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.components.MetaClassAttributesTableModel
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.components.MetaClassSyncButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.model.MetaClassListItem
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.ScrollPaneConstants

class MetaClassToolWindowPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val storageService = project.service<MetaClassStorageService>()
    private val messageBus = project.messageBus.connect(this)

    private val syncButton = MetaClassSyncButton()
    private val statusLabel = JBLabel().apply {
        border = JBUI.Borders.emptyTop(8)
    }
    private val selectorLabel = JBLabel(MessageBundle.message("metaclass.tool_window.selector.label"))
    private val metaClassCombo = ComboBox<MetaClassListItem>().apply {
        maximumRowCount = 20
    }
    private val tableModel = MetaClassAttributesTableModel()
    private val table = JBTable(tableModel).apply {
        autoResizeMode = JTable.AUTO_RESIZE_OFF
        setShowGrid(true)
        rowHeight = JBUI.scale(22)
        for (columnIndex in 0 until columnModel.columnCount) {
            columnModel.getColumn(columnIndex).preferredWidth = JBUI.scale(
                when (columnIndex) {
                    0 -> 140
                    1 -> 220
                    2 -> 160
                    3 -> 180
                    else -> 90
                },
            )
        }
        MetaClassAttributesTableCopySupport.install(this)
    }
    private val tableScrollPane = JBScrollPane(
        table,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED,
    )

    private var metaClasses: List<MetaClassDto> = emptyList()
    private var selectedFullCode: String? = null

    init {
        border = JBUI.Borders.empty(8)
        layout = BorderLayout(JBUI.scale(0), JBUI.scale(8))

        val topPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            add(syncButton)
            add(statusLabel)
            add(Box.createVerticalStrut(JBUI.scale(8)))
            add(selectorLabel)
            add(Box.createVerticalStrut(JBUI.scale(4)))
            add(metaClassCombo)
        }
        add(topPanel, BorderLayout.NORTH)
        add(tableScrollPane, BorderLayout.CENTER)

        metaClassCombo.addActionListener { onMetaClassSelected() }
        ComboboxSpeedSearch.installSpeedSearch(metaClassCombo) { item ->
            item?.let { "${it.title} ${it.fullCode}" }.orEmpty()
        }

        messageBus.subscribe(BackgroundTaskTopic.TOPIC, object : BackgroundTaskTopic {
            override fun taskStarted(task: AbstractBackgroundTask) {
                if (task is MetaClassSyncBackgroundTask) {
                    ApplicationManager.getApplication().invokeLater { syncButton.setSyncEnabled(false) }
                }
            }

            override fun taskFinished(task: AbstractBackgroundTask) {
                if (task is MetaClassSyncBackgroundTask) {
                    ApplicationManager.getApplication().invokeLater {
                        syncButton.setSyncEnabled(true)
                        reloadMetaClasses()
                    }
                }
            }
        })

        reloadMetaClasses()
    }

    private fun reloadMetaClasses() {
        metaClasses = storageService.load().sortedBy { it.title.lowercase() }
        updateStatusLabel()
        reloadSelector()
        updateAttributesTable()
    }

    private fun updateStatusLabel() {
        statusLabel.text = if (metaClasses.isEmpty()) {
            MessageBundle.message("metaclass.tool_window.status.empty")
        } else {
            MessageBundle.message("metaclass.tool_window.status.loaded", metaClasses.size)
        }
    }

    private fun reloadSelector() {
        val items = metaClasses.map(MetaClassListItem::fromDto)
        metaClassCombo.removeAllItems()
        items.forEach(metaClassCombo::addItem)
        if (items.isEmpty()) {
            selectedFullCode = null
            return
        }
        val restoreIndex = items.indexOfFirst { it.fullCode == selectedFullCode }
        metaClassCombo.selectedIndex = if (restoreIndex >= 0) restoreIndex else 0
        selectedFullCode = (metaClassCombo.selectedItem as? MetaClassListItem)?.fullCode
    }

    private fun onMetaClassSelected() {
        selectedFullCode = (metaClassCombo.selectedItem as? MetaClassListItem)?.fullCode
        updateAttributesTable()
    }

    private fun updateAttributesTable() {
        val metaClass = metaClasses.find { it.fullCode == selectedFullCode }
        tableModel.setAttributes(metaClass?.attributes.orEmpty().sortedBy { it.code })
    }

    override fun dispose() {
        messageBus.disconnect()
    }
}
