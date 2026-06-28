package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.components

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.table.JBTable
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.KeyStroke

object MetaClassAttributesTableCopySupport {

    fun install(table: JBTable) {
        table.cellSelectionEnabled = true

        val copyAction = ActionListener { copySelection(table) }

        table.registerKeyboardAction(
            copyAction,
            "Copy",
            KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
            JComponent.WHEN_FOCUSED,
        )

        table.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(event: MouseEvent) = showPopupIfNeeded(event)
            override fun mouseReleased(event: MouseEvent) = showPopupIfNeeded(event)

            private fun showPopupIfNeeded(event: MouseEvent) {
                if (!event.isPopupTrigger || table.selectedRow < 0) return
                JBPopupMenu()
                    .apply {
                        add(MessageBundle.message("metaclass.tool_window.table.copy"))
                            .addActionListener(copyAction)
                    }
                    .show(event.component, event.x, event.y)
            }
        })
    }

    private fun copySelection(table: JBTable) {
        val rows = table.selectedRows
        val columns = table.selectedColumns
        if (rows.isEmpty() || columns.isEmpty()) return

        val text = buildString {
            rows.forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) append('\n')
                columns.forEachIndexed { columnIndex, column ->
                    if (columnIndex > 0) append('\t')
                    append(table.getValueAt(row, column)?.toString().orEmpty())
                }
            }
        }
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }
}
