package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.components

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.SelectedSrcOption
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.CompletableFuture
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import javax.swing.table.AbstractTableModel

/**
 * Поле выбора исходников в запрос (для включения или исключения)
 */
class MultiSelectField(
    private val project: Project,
    private val chooserTitle: String,
    initialValues: List<SelectedSrcOption>,
    private val optionsProvider: () -> CompletableFuture<List<SrcOption>>,
    private val onLoadError: (Throwable) -> Unit,
) {
    private data class SelectedRow(val title: String, val code: String)

    private val selectedValues = LinkedHashMap<String, SelectedSrcOption>().apply {
        initialValues.forEach { option ->
            val code = option.code.trim()
            if (code.isNotEmpty()) {
                put(code, SelectedSrcOption(code, option.title))
            }
        }
    }
    private val chooseButton = JButton(MessageBundle.message("sync.dialog.multi.select.button"))
    private val changeListeners = mutableListOf<() -> Unit>()
    private var cachedOptions: List<SrcOption> = emptyList()
    private val tableModel = SelectedTableModel()
    private val previewTable = JBTable(tableModel).apply {
        tableHeader.reorderingAllowed = false
        setShowGrid(false)
        setRowSelectionAllowed(false)
        fillsViewportHeight = true
    }
    private val scrollPane = JBScrollPane(previewTable).apply {
        preferredSize = Dimension(420, 108)
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        alignmentX = JComponent.LEFT_ALIGNMENT
    }

    init {
        chooseButton.addActionListener {
            chooseButton.isEnabled = false
            optionsProvider.invoke()
                .whenComplete { options, error ->
                    ApplicationManager.getApplication().invokeLater({
                        chooseButton.isEnabled = true
                        if (project.isDisposed) return@invokeLater
                        if (error != null) {
                            onLoadError(unwrapCompletionError(error))
                            return@invokeLater
                        }

                        cachedOptions = options.orEmpty()
                        updateInfoLabel()
                        val chooserDialog = ValuesSelectDialog(
                            project = project,
                            titleText = chooserTitle,
                            values = cachedOptions,
                            initialSelectionCodes = selectedValues.keys.toList(),
                        )
                        if (chooserDialog.showAndGet()) {
                            selectedValues.clear()
                            chooserDialog.getSelectedOptions().forEach { option ->
                                selectedValues[option.code] = option
                            }
                            updateInfoLabel()
                            notifyChanged()
                        }
                    }, ModalityState.stateForComponent(chooseButton))
                }
        }
        updateInfoLabel()
    }

    fun getValues(): List<SelectedSrcOption> = selectedValues.values.toList()

    fun addChangeListener(listener: () -> Unit) {
        changeListeners += listener
    }

    fun clear() {
        selectedValues.clear()
        updateInfoLabel()
    }

    fun setSelectionEnabled(enabled: Boolean) {
        chooseButton.isEnabled = enabled
        previewTable.isEnabled = enabled
    }

    fun createSectionComponent(labelText: String): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = JComponent.LEFT_ALIGNMENT
            add(
                JPanel(BorderLayout(8, 0)).apply {
                    alignmentX = JComponent.LEFT_ALIGNMENT
                    add(JLabel(labelText), BorderLayout.WEST)
                    add(chooseButton, BorderLayout.EAST)
                    maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
                }
            )
            add(Box.createVerticalStrut(6))
            add(scrollPane)
            maximumSize = Dimension(Int.MAX_VALUE, 140)
        }
    }

    fun notifyChanged() {
        changeListeners.forEach { it.invoke() }
    }

    private fun updateInfoLabel() {
        tableModel.setRows(buildRows())
    }

    private fun buildRows(): List<SelectedRow> {
        val optionMap = cachedOptions
            .associateBy({ it.code }, { it.title })
        return selectedValues.values.map { option ->
            val resolvedTitle = optionMap[option.code].orEmpty().ifBlank { option.title }
            SelectedRow(
                title = resolvedTitle,
                code = option.code
            )
        }
    }

    private fun unwrapCompletionError(error: Throwable): Throwable {
        return error.cause ?: error
    }

    private class SelectedTableModel : AbstractTableModel() {
        private val rows = mutableListOf<SelectedRow>()

        override fun getRowCount(): Int = rows.size

        override fun getColumnCount(): Int = 2

        override fun getColumnName(column: Int): String {
            return if (column == 0) {
                MessageBundle.message("sync.dialog.multi.select.column.title")
            } else {
                MessageBundle.message("sync.dialog.multi.select.column.code")
            }
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val row = rows[rowIndex]
            return if (columnIndex == 0) row.title else row.code
        }

        fun setRows(newRows: List<SelectedRow>) {
            rows.clear()
            rows.addAll(newRows)
            fireTableDataChanged()
        }
    }
}
