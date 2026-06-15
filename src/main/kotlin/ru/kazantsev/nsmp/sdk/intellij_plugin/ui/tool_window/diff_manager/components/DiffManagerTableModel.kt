package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.components

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffRow
import javax.swing.table.AbstractTableModel

class DiffManagerTableModel : AbstractTableModel() {
    private val rows = mutableListOf<DiffRow>()


    private val columns = listOf(
        MessageBundle.message("sync.command.diff.manager.table.type"),
        MessageBundle.message("sync.command.diff.manager.table.code"),
        MessageBundle.message("sync.command.diff.manager.table.status"),
    )

    override fun getRowCount(): Int = rows.size

    override fun getColumnCount(): Int = columns.size

    override fun getColumnName(column: Int): String = columns[column]

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val row = rows[rowIndex]
        return when (columnIndex) {
            0 -> row.type.code
            1 -> row.code
            2 -> row.status.localizedName()
            else -> ""
        }
    }

    fun setRows(values: List<DiffRow>) {
        rows.clear()
        rows.addAll(values)
        fireTableDataChanged()
    }

    fun rowAt(index: Int): DiffRow? = rows.getOrNull(index)
}
