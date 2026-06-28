package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.util.Disposer
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService

/**
 * Tool Window с синхронизацией метаинформации и просмотром атрибутов метакласса.
 */
class MetaClassToolWindowFactory : ToolWindowFactory, DumbAware {

    companion object {
        const val TOOL_WINDOW_ID = "NSMP SDK Meta Classes"
        const val ACTION_PLACE = "NSMP_SDK.MetaClassToolWindow"
    }

    override fun shouldBeAvailable(project: Project): Boolean =
        project.service<PluginActivationService>().pluginActive

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MetaClassToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        Disposer.register(content, panel)
        toolWindow.contentManager.addContent(content)
    }
}
