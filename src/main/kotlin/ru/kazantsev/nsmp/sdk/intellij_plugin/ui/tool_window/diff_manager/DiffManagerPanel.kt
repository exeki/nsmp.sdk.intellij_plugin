package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.CompareSetType
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffRow
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Files
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.TableRowSorter

class DiffManagerPanel(
    private val project: Project,
    private val onCompareSetsChanged: (CompareSetType, CompareSetType) -> Unit,
    private val onUpdateRemoteClicked: () -> Unit
) : JPanel(BorderLayout()) {
    private val model = DiffManagerTableModel()
    private val sorter = TableRowSorter(model)
    private val leftLabel = JLabel(MessageBundle.message("sync.command.local.changes.compare.left"))
    private val leftSetFilter = ComboBox(CompareSetType.entries.toTypedArray()).apply {
        addActionListener { handleCompareSetChange() }
        alignmentY = CENTER_ALIGNMENT
        renderer = DefaultListCellRenderer().apply {
            horizontalAlignment = JLabel.LEFT
        }
    }
    private val rightLabel = JLabel(MessageBundle.message("sync.command.local.changes.compare.right"))
    private val rightSetFilter = ComboBox(CompareSetType.entries.toTypedArray()).apply {
        addActionListener { handleCompareSetChange() }
    }
    private val updateRemoteButton = JButton(
        MessageBundle.message("sync.command.local.changes.remote.update")
    ).apply {
        addActionListener { onUpdateRemoteClicked() }
    }
    private val typeLabel = JLabel(MessageBundle.message("sync.command.local.changes.table.type"))
    private val typeFilterField = ComboBox(arrayOf("ALL", "SCRIPT", "MODULE", "ADV_IMPORT")).apply {
        addActionListener { applyFilters() }
    }
    private val sourceCodeLabel = JLabel(MessageBundle.message("sync.command.local.changes.table.code"))
    private val sourceCodeFilterField = JBTextField(18).apply {
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = applyFilters()
            override fun removeUpdate(e: DocumentEvent?) = applyFilters()
            override fun changedUpdate(e: DocumentEvent?) = applyFilters()
        })
    }
    private var updatingCompareSetControls = false
    private val table = JBTable(model).apply {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        rowSorter = sorter
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (selectedRow < 0) return
                openDiffForSelectedRow()
            }
        })
    }

    init {
        border = JBUI.Borders.empty()
        add(createTopPanel(), BorderLayout.NORTH)
        add(JBScrollPane(table), BorderLayout.CENTER)
    }

    fun setRows(rows: List<DiffRow>) {
        model.setRows(rows)
        applyFilters()
    }

    override fun add(component : Component): Component? {
        if(component is JComponent) component.alignmentY = CENTER_ALIGNMENT
        return super.add(component)
    }

    private fun createTopPanel(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            add(leftLabel)
            add(leftSetFilter)
            add(rightLabel)
            add(rightSetFilter)
            add(updateRemoteButton)
            add(typeLabel)
            add(typeFilterField)
            add(sourceCodeLabel)
            add(sourceCodeFilterField)
        }
    }

    fun setCompareSets(left: CompareSetType, right: CompareSetType) {
        updatingCompareSetControls = true
        leftSetFilter.selectedItem = left
        rightSetFilter.selectedItem = right
        updatingCompareSetControls = false
    }

    fun getCompareSets(): Pair<CompareSetType, CompareSetType> {
        val left = (leftSetFilter.selectedItem as? CompareSetType) ?: CompareSetType.LOCAL
        val right = (rightSetFilter.selectedItem as? CompareSetType) ?: CompareSetType.LAST_SYNC
        return left to right
    }

    private fun handleCompareSetChange() {
        if (updatingCompareSetControls) return
        val (left, right) = getCompareSets()
        onCompareSetsChanged(left, right)
    }

    private fun applyFilters() {
        val codeText = sourceCodeFilterField.text.trim()
        val selectedType = (typeFilterField.selectedItem as? String).orEmpty()

        val filters = mutableListOf<RowFilter<DiffManagerTableModel, Int>>()
        if (selectedType.isNotBlank() && selectedType != "ALL") {
            filters += RowFilter.regexFilter("^${Pattern.quote(selectedType.lowercase())}$", 0)
        }
        if (codeText.isNotBlank()) {
            filters += RowFilter.regexFilter("(?i)${Pattern.quote(codeText)}", 1)
        }

        sorter.rowFilter = if (filters.isEmpty()) null else RowFilter.andFilter(filters)
    }

    private fun openDiffForSelectedRow() {
        val selected = table.selectedRow
        if (selected < 0) return
        val modelRow = table.convertRowIndexToModel(selected)
        val row = model.rowAt(modelRow) ?: return
        val beforeText = row.rightPath
            ?.takeIf(Files::exists)
            ?.let(Files::readString)
            .orEmpty()
        val afterText = row.leftPath
            .takeIf(Files::exists)
            ?.let(Files::readString)
            .orEmpty()

        val request = SimpleDiffRequest(
            "${row.type.code}: ${row.code}",
            DiffContentFactory.getInstance().create(beforeText),
            DiffContentFactory.getInstance().create(afterText),
            row.rightSet.uiCode,
            row.leftSet.uiCode
        )
        DiffManager.getInstance().showDiff(project, request)
    }
}
