package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.CheckButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.PullButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons.PushButton
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Боковое меню справа
 */
class MainToolWindowFactory : ToolWindowFactory, DumbAware {

    companion object {
        const val ACTION_PLACE = "NSMP_SDK.MainTollWindow"
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return project.service<PluginActivationService>().pluginActive
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val installationInfoPanel = InstallationInfoPanel(project).also {
            project.service<PluginActivationService>().mainToolWindowInstallationInfoPanel = it
        }
        val serverStatePanel = ServerStatePanel(project)

        val statusPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            add(installationInfoPanel)
            add(serverStatePanel)
        }

        val actionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            //add(OpenInstallationButton())
            //add(Box.createVerticalStrut(8))
            add(PullButton())
            add(Box.createVerticalStrut(8))
            add(CheckButton())
            add(Box.createVerticalStrut(8))
            add(PushButton())
            add(Box.createVerticalGlue())
        }

        val panel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            add(statusPanel, BorderLayout.NORTH)
            add(actionsPanel, BorderLayout.CENTER)
            add(AuthorFooter(), BorderLayout.SOUTH)
        }

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}
