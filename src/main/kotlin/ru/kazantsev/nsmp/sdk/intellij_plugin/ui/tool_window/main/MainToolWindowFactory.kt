package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.PullButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.PushButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.CheckButton
import javax.swing.*

/**
 * Боковое меню справа
 */
class MainToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            add(Box.createVerticalStrut(5))
            add(PullButton())
            add(Box.createVerticalStrut(8))
            add(CheckButton())
            add(Box.createVerticalStrut(8))
            add(PushButton())
            add(Box.createVerticalGlue())
        }

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
    /*
    private fun createSourcesSyncGroup(project: Project): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(createGroupHeader(MessageBundle.message("group.sources.sync.title")))
            add(PullButton())
            add(Box.createVerticalStrut(8))
            add(CheckButton())
            add(Box.createVerticalStrut(8))
            add(PushButton())
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
     */
}
