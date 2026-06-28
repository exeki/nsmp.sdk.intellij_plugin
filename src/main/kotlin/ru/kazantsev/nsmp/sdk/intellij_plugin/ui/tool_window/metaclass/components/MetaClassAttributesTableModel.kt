package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.components

import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.AttributeDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import javax.swing.table.AbstractTableModel

class MetaClassAttributesTableModel : AbstractTableModel() {

    private var rows: List<AttributeDto> = emptyList()

    fun setAttributes(attributes: List<AttributeDto>) {
        rows = attributes
        fireTableDataChanged()
    }

    override fun getRowCount(): Int = rows.size

    override fun getColumnCount(): Int = COLUMN_KEYS.size

    override fun getColumnName(column: Int): String =
        MessageBundle.message(COLUMN_KEYS[column])

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val attribute = rows[rowIndex]
        return when (columnIndex) {
            0 -> attribute.code
            1 -> attribute.title
            2 -> attribute.resolvedType().title
            3 -> attribute.relatedMetaClass.orEmpty()
            4 -> attribute.required
            5 -> attribute.hardcoded
            else -> null
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> =
        when (columnIndex) {
            4, 5 -> java.lang.Boolean::class.java
            else -> String::class.java
        }

    private companion object {
        val COLUMN_KEYS = listOf(
            "metaclass.tool_window.table.column.code",
            "metaclass.tool_window.table.column.title",
            "metaclass.tool_window.table.column.type",
            "metaclass.tool_window.table.column.related",
            "metaclass.tool_window.table.column.required",
            "metaclass.tool_window.table.column.hardcoded",
        )
    }
}
