package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.DiffService

class DiffManagerToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val diffService = project.getService(DiffService::class.java)
        val panel = DiffManagerPanel(
            project = project,
            onCompareSetsChanged = { left, right -> diffService.showLocalChanges(left, right) },
            onUpdateRemoteClicked = { diffService.updateRemoteStateAndRefreshIfNeeded() }
        )
        diffService.bindPanel(panel)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}
