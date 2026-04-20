package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.components

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.OptionRow
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.SelectedSrcOption
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Диалог для выбора значений в списки на включение в запрос или в список на исключение
 */
class ValuesSelectDialog(
    project: Project,
    titleText: String,
    values: List<SrcOption>,
    initialSelectionCodes: List<String>,
) : DialogWrapper(project) {

    private val allRowsByCode: LinkedHashMap<String, OptionRow> = LinkedHashMap<String, OptionRow>().apply {
        values.forEach { option ->
            val code = option.code.trim()
            if (code.isEmpty() || containsKey(code)) return@forEach
            put(code, OptionRow(title = option.title.trim(), code = code))
        }
    }
    private val allRows: List<OptionRow> = allRowsByCode.values.sortedWith(compareBy<OptionRow> { it.title }.thenBy { it.code })
    private val selectedCodes = LinkedHashSet<String>(initialSelectionCodes)

    private val availableTableModel = OptionTableModel()
    private val selectedTableModel = OptionTableModel()
    private val availableTable = JBTable(availableTableModel).apply {
        selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
    }
    private val selectedTable = JBTable(selectedTableModel).apply {
        selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
    }
    private val searchField = SearchTextField()
    private val stateLabel = JBLabel()

    init {
        title = titleText
        init()
        searchField.textEditor.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = rebuildList()
            override fun removeUpdate(e: DocumentEvent) = rebuildList()
            override fun changedUpdate(e: DocumentEvent) = rebuildList()
        })
        availableTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount != 2 || e.button != MouseEvent.BUTTON1) return
                val rowIndex = availableTable.rowAtPoint(e.point)
                val code = availableTableModel.rowAt(rowIndex)?.code ?: return
                selectedCodes.add(code)
                rebuildList()
            }
        })
        selectedTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount != 2 || e.button != MouseEvent.BUTTON1) return
                val rowIndex = selectedTable.rowAtPoint(e.point)
                val code = selectedTableModel.rowAt(rowIndex)?.code ?: return
                selectedCodes.remove(code)
                rebuildList()
            }
        })
        rebuildList()
    }

    override fun createCenterPanel(): JComponent {
        val addSelected = JButton(MessageBundle.message("sync.dialog.multi.select.add.one"))
        val removeSelected = JButton(MessageBundle.message("sync.dialog.multi.select.remove.one"))
        val addFiltered = JButton(MessageBundle.message("sync.dialog.multi.select.add.filtered"))
        val removeAll = JButton(MessageBundle.message("sync.dialog.multi.select.remove.all"))
        val hintLabel =
            JBLabel("<html><i>${MessageBundle.message("sync.dialog.multi.select.double.click.hint")}</i></html>")

        addSelected.addActionListener {
            selectedCodes.addAll(
                availableTable.selectedRows
                    .toList()
                    .mapNotNull { rowIndex -> availableTableModel.rowAt(rowIndex)?.code }
            )
            rebuildList()
        }
        removeSelected.addActionListener {
            selectedCodes.removeAll(
                selectedTable.selectedRows
                    .toList()
                    .mapNotNull { rowIndex -> selectedTableModel.rowAt(rowIndex)?.code }
                    .toSet()
            )
            rebuildList()
        }
        addFiltered.addActionListener {
            selectedCodes.addAll(filteredAvailableRows().map { it.code })
            rebuildList()
        }
        removeAll.addActionListener {
            selectedCodes.clear()
            rebuildList()
        }

        val addPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(addSelected)
            add(Box.createHorizontalStrut(6))
            add(addFiltered)
            alignmentX = JComponent.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }

        val removePanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(removeSelected)
            add(Box.createHorizontalStrut(6))
            add(removeAll)
            alignmentX = JComponent.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }

        val availablePanel = JPanel(BorderLayout(0, 6)).apply {
            add(JBLabel(MessageBundle.message("sync.dialog.multi.select.available")), BorderLayout.NORTH)
            add(JBScrollPane(availableTable), BorderLayout.CENTER)
            alignmentX = JComponent.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }
        val selectedPanel = JPanel(BorderLayout(0, 6)).apply {
            add(
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                    alignmentX = JComponent.LEFT_ALIGNMENT
                    add(stateLabel.apply {
                        alignmentX = JComponent.LEFT_ALIGNMENT
                    })
                },
                BorderLayout.NORTH
            )
            add(JBScrollPane(selectedTable), BorderLayout.CENTER)
            alignmentX = JComponent.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }

        val centerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = JComponent.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            add(availablePanel)
            add(Box.createVerticalStrut(8))
            add(addPanel)
            add(Box.createVerticalStrut(8))
            add(selectedPanel)
            add(Box.createVerticalStrut(8))
            add(removePanel)
        }

        val searchHeight = searchField.preferredSize.height

        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            preferredSize = Dimension(760, 520)
            add(searchField.apply {
                text = ""
                alignmentX = JComponent.LEFT_ALIGNMENT
                maximumSize = Dimension(Int.MAX_VALUE, searchHeight)
                preferredSize = Dimension(preferredSize.width, searchHeight)
            })
            add(Box.createVerticalStrut(6))
            add(hintLabel.apply {
                alignmentX = JComponent.LEFT_ALIGNMENT
            })
            add(Box.createVerticalStrut(6))
            add(centerPanel)
        }
    }

    fun getSelectedOptions(): List<SelectedSrcOption> {
        return selectedRows().map { SelectedSrcOption(code = it.code, title = it.title) }
    }

    private fun rebuildList() {
        availableTableModel.setRows(filteredAvailableRows())
        selectedTableModel.setRows(selectedRows())

        stateLabel.text = MessageBundle.message(
            "sync.dialog.multi.select.state",
            selectedCodes.size,
            allRows.size
        )
    }

    private fun filteredAvailableRows(): List<OptionRow> {
        val query = searchField.text.trim()
        val available = allRows.filterNot { selectedCodes.contains(it.code) }
        if (query.isEmpty()) return available
        return available.filter {
            it.code.contains(query, ignoreCase = true) || it.title.contains(query, ignoreCase = true)
        }
    }

    private fun selectedRows(): List<OptionRow> {
        return selectedCodes.map { code ->
            allRowsByCode[code] ?: OptionRow(title = "", code = code)
        }.sortedWith(compareBy<OptionRow> { it.title }.thenBy { it.code })
    }

}