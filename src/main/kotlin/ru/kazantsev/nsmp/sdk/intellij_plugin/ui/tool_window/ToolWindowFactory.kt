package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons.PullButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons.PushButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons.ShowLocalChangesButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons.SyncCheckButton
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

/**
 * Боковое меню справа
 */
class ToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            add(Box.createVerticalStrut(10))
            add(createSourcesSyncGroup(project))
            add(Box.createVerticalGlue())
        }

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createSourcesSyncGroup(project: Project): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(createGroupHeader(MessageBundle.message("group.sources.sync.title")))
            add(PullButton(project))
            add(Box.createVerticalStrut(8))
            add(SyncCheckButton(project))
            add(Box.createVerticalStrut(8))
            add(PushButton(project))
            add(Box.createVerticalStrut(8))
            add(ShowLocalChangesButton(project))
        }
    }

    private fun createGroupHeader(title: String): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(JSeparator().apply {
                alignmentX = Component.LEFT_ALIGNMENT
                maximumSize = Dimension(Int.MAX_VALUE, 1)
            })
            add(Box.createVerticalStrut(6))
            add(JLabel(title).apply { alignmentX = Component.LEFT_ALIGNMENT })
            add(Box.createVerticalStrut(8))
        }
    }
}
