package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.components

import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.OptionRow
import javax.swing.table.AbstractTableModel

/**
 * Таблица с опциями
 */
class OptionTableModel : AbstractTableModel() {
    private val rows = mutableListOf<OptionRow>()

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

    fun setRows(newRows: List<OptionRow>) {
        rows.clear()
        rows.addAll(newRows)
        fireTableDataChanged()
    }

    fun rowAt(index: Int): OptionRow? {
        if (index !in 0 until rows.size) return null
        return rows[index]
    }
}
