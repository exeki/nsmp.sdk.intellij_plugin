package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffRow
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.table.AbstractTableModel

class DiffManagerTableModel : AbstractTableModel() {
    private val rows = mutableListOf<DiffRow>()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    private val columns = listOf(
        MessageBundle.message("sync.command.local.changes.table.type"),
        MessageBundle.message("sync.command.local.changes.table.code"),
        MessageBundle.message("sync.command.local.changes.table.status"),
        MessageBundle.message("sync.command.local.changes.table.last.sync.date"),
    )

    override fun getRowCount(): Int = rows.size

    override fun getColumnCount(): Int = columns.size

    override fun getColumnName(column: Int): String = columns[column]

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val row = rows[rowIndex]
        return when (columnIndex) {
            0 -> row.type.code
            1 -> row.code
            2 -> row.status.name
            3 -> row.lastSyncDate?.let(dateFormatter::format).orEmpty()
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
