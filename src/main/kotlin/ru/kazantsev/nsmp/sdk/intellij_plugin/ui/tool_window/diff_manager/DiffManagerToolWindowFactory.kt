package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.components.DiffManagerTable

class DiffManagerToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project): Boolean {
        return project.service<PluginActivationService>().pluginActive
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = DiffManagerTable(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}
