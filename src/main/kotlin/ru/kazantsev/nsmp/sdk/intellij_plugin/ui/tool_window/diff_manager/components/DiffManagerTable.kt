package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.components

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectRemoteStateUpdateAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskTopic
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data.DiffStatus
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data.SrcState
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state.DiffService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.AbstractBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.UpdateRemoteStateBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.DiffManagerToolWindowFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffRow
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Files
import java.util.regex.Pattern
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.RowFilter
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.TableRowSorter

class DiffManagerTable(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val messageBus = project.messageBus.connect(this)
    private var updatingCompareSetControls = false
    private var refreshGeneration = 0L
    private val panelDisposable = Disposer.newDisposable("DiffManagerTable.loadingPanel")
    private val diffService = project.service<DiffService>()
    private val topPanel = DiffManagerTopPanel()
    private val model = DiffManagerTableModel()
    private val sorter = TableRowSorter(model)
    private val table = JBTable(model).apply {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        rowSorter = sorter
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (selectedRow < 0) return
                openDiffForSelectedRowAsync()
            }
        })
    }
    private val loadingPanel = JBLoadingPanel(BorderLayout(), panelDisposable).apply {
        add(JBScrollPane(table), BorderLayout.CENTER)
    }

    init {
        border = JBUI.Borders.empty()
        topPanel.apply {
            leftStateFilter.addActionListener { refresh() }
            rightStateFilter.addActionListener { refresh() }
            refreshButton.addActionListener { refresh() }
            updateRemoteButton.addActionListener { onUpdateRemoteClicked() }
            typeFilterField.addActionListener { applyFilters() }
            codeFilterField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = applyFilters()
                override fun removeUpdate(e: DocumentEvent?) = applyFilters()
                override fun changedUpdate(e: DocumentEvent?) = applyFilters()
            })
        }
        messageBus.subscribe(BackgroundTaskTopic.TOPIC, object : BackgroundTaskTopic {
            override fun taskFinished(task: AbstractBackgroundTask) {
                if (project.isDisposed) return
                if (task is UpdateRemoteStateBackgroundTask) ApplicationManager.getApplication().invokeLater {
                    if (project.isDisposed) return@invokeLater
                    if (task.showLastSyncToRemoteInDiffManager) updateCompareSet(SrcState.LAST_SYNC, SrcState.REMOTE)
                    if (isRemoteInCompareSet()) refresh()
                }
            }
        })
        add(topPanel, BorderLayout.NORTH)
        add(loadingPanel, BorderLayout.CENTER)
        refresh()
    }

    fun refresh() {
        if (updatingCompareSetControls) return
        updatingCompareSetControls = true
        val left = (topPanel.leftStateFilter.selectedItem as? SrcState) ?: SrcState.CURRENT
        val right = (topPanel.rightStateFilter.selectedItem as? SrcState) ?: SrcState.LAST_SYNC
        val req = SrcRequest(allScripts = true, allModules = true, allAdvImports = true)
        val currentGeneration = ++refreshGeneration
        loadingPanel.startLoading()
        AppExecutorUtil.getAppExecutorService().execute {
            var diffRows: List<DiffRow> = emptyList()
            var error: Throwable? = null
            try {
                val rows = mutableListOf<DiffRow>()
                diffService.getDiffPairs(req, left, right)
                    .forEach { (type, pairs) ->
                        pairs
                            .filter { it.diffStatus != DiffStatus.UNCHANGED }
                            .forEach { pair ->
                                rows.add(
                                    DiffRow(
                                        type = type,
                                        code = pair.code,
                                        leftSet = left,
                                        leftFile = pair.left?.virtualFile,
                                        rightSet = right,
                                        rightFile = pair.right?.virtualFile,
                                        status = pair.diffStatus
                                    )
                                )
                            }
                    }
                diffRows = rows
            } catch (t: Throwable) {
                error = t
            } finally {
                //TODO какая то херня, надо упростить
                ApplicationManager.getApplication().invokeLater {
                    if (project.isDisposed) return@invokeLater
                    if (currentGeneration == refreshGeneration) {
                        val failure = error
                        if (failure != null) {
                            Messages.showErrorDialog(
                                project,
                                failure.message ?: "Failed to load diff rows.",
                                "Diff Manager"
                            )
                        } else {
                            setRows(diffRows)
                        }
                        updatingCompareSetControls = false
                        loadingPanel.stopLoading()
                    }
                }
            }
        }
    }

    fun onUpdateRemoteClicked() {
        ActionManager.getInstance().tryToExecute(
            ProjectRemoteStateUpdateAction(),
            null,
            this,
            DiffManagerToolWindowFactory.ACTION_PLACE,
            true
        )
    }

    fun setRows(rows: List<DiffRow>) {
        model.setRows(rows)
        applyFilters()
    }


    override fun add(component: Component): Component? {
        if (component is JComponent) component.alignmentY = CENTER_ALIGNMENT
        return super.add(component)
    }

    override fun removeNotify() {
        super.removeNotify()
        Disposer.dispose(panelDisposable)
    }

    fun applyFilters() {
        val codeText = topPanel.codeFilterField.text.trim()
        val selectedType =
            topPanel.typeFilterField.selectedItem as? ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffTypeFilter

        val filters = mutableListOf<RowFilter<DiffManagerTableModel, Int>>()
        if (selectedType != null && selectedType != ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffTypeFilter.ALL) {
            filters += RowFilter.regexFilter("^${Pattern.quote(selectedType.code)}$", 0)
        }
        if (codeText.isNotBlank()) {
            filters += RowFilter.regexFilter("(?i)${Pattern.quote(codeText)}", 1)
        }

        sorter.rowFilter = if (filters.isEmpty()) null else RowFilter.andFilter(filters)
    }

    private fun openDiffForSelectedRowAsync() {
        val selected = table.selectedRow
        if (selected < 0) return
        val modelRow = table.convertRowIndexToModel(selected)
        val row = model.rowAt(modelRow) ?: return
        AppExecutorUtil.getAppExecutorService().execute {
            try {
                val leftText = readText(row.leftFile)
                val rightText = readText(row.rightFile)
                ApplicationManager.getApplication().invokeLater {
                    if (project.isDisposed) return@invokeLater
                    val request = SimpleDiffRequest(
                        "${row.type.code}: ${row.code}",
                        DiffContentFactory.getInstance().create(leftText),
                        DiffContentFactory.getInstance().create(rightText),
                        row.leftSet.code,
                        row.rightSet.code
                    )
                    DiffManager.getInstance().showDiff(project, request)
                }
            } catch (t: Throwable) {
                ApplicationManager.getApplication().invokeLater {
                    if (project.isDisposed) return@invokeLater
                    Messages.showErrorDialog(
                        project,
                        t.message ?: "Failed to load compared files.",
                        "Diff Manager"
                    )
                }
            }
        }
    }

    private fun readText(file: VirtualFile?): String = file
        ?.takeIf(VirtualFile::exists)
        ?.let(VirtualFile::toNioPath)
        ?.let(Files::readString)
        .orEmpty()

    private fun isRemoteInCompareSet(): Boolean {
        val left = topPanel.leftStateFilter.selectedItem as? SrcState
        val right = topPanel.rightStateFilter.selectedItem as? SrcState
        return left == SrcState.REMOTE || right == SrcState.REMOTE
    }

    @Suppress("SameParameterValue")
    private fun updateCompareSet(left: SrcState, right: SrcState) {
        updatingCompareSetControls = true
        try {
            topPanel.leftStateFilter.selectedItem = left
            topPanel.rightStateFilter.selectedItem = right
        } finally {
            updatingCompareSetControls = false
        }
    }

    override fun dispose() {
        messageBus.dispose()
    }
}
