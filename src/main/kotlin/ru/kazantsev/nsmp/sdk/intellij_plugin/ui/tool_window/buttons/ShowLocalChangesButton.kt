package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons

import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.local_changes.LocalSourceChangesDialog
import java.awt.Dimension
import javax.swing.JButton

class ShowLocalChangesButton(
    private val project: Project
) : JButton(MessageBundle.message("sync.command.local.changes.short.title")) {

    init {
        alignmentX = LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        addActionListener {
            LocalSourceChangesDialog(project).show()
        }
    }
}
