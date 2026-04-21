package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.local_changes

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

class LocalSourceChangesListCellRenderer : ColoredListCellRenderer<LocalFileChange>() {
    override fun customizeCellRenderer(
        list: JList<out LocalFileChange>,
        value: LocalFileChange?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        if (value == null) return
        append("[${value.group}] ", SimpleTextAttributes.GRAY_ATTRIBUTES)
        append("${value.type} ", attributes(value.type))
        append(value.relativePath)
    }

    private fun attributes(type: LocalFileChangeType): SimpleTextAttributes {
        return when (type) {
            LocalFileChangeType.ADDED -> SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
            LocalFileChangeType.DELETED -> SimpleTextAttributes.ERROR_ATTRIBUTES
            LocalFileChangeType.MODIFIED -> SimpleTextAttributes.LINK_ATTRIBUTES
        }
    }
}
