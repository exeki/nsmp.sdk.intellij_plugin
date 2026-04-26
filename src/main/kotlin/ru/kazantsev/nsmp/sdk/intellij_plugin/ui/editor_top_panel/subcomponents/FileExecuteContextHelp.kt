package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents

import com.intellij.icons.AllIcons
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JLabel

class FileExecuteContextHelp : JLabel(AllIcons.General.ContextHelp) {
    override fun getToolTipLocation(event: MouseEvent?): Point {
        return Point(0, height + 8)
    }

    private fun htmlTooltip(text: String): String {
        return "<html><body width='420'>$text</body></html>"
    }

    init{
        toolTipText = htmlTooltip(MessageBundle.message("sync.command.execute.context.help"))
        alignmentY = CENTER_ALIGNMENT
    }
}