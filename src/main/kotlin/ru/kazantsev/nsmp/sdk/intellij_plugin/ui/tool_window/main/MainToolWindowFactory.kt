package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.CheckButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.OpenInstallationButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.PullButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.PushButton
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Боковое меню справа
 */
class MainToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project): Boolean {
        return project.service<PluginActivationService>().pluginActive
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            add(Box.createVerticalStrut(5))
            add(OpenInstallationButton())
            add(Box.createVerticalStrut(8))
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
}
