package ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

@Service(Service.Level.PROJECT)
class PluginActivationService(private val project: Project) {

    val projectSettings = project.service<ProjectSettingsService>()

    var pluginActive: Boolean = projectSettings.installationIsSpecified()

    fun refresh() {
        val installationIsSpecified = project.service<ProjectSettingsService>().installationIsSpecified()
        pluginActive = installationIsSpecified
        refreshToolWindows()
        refreshTopPanels()
    }

    private fun refreshToolWindows() {
        ApplicationManager.getApplication().invokeLater {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            TOOL_WINDOW_IDS.forEach { toolWindowId ->
                toolWindowManager.getToolWindow(toolWindowId)?.let { toolWindow ->
                    toolWindow.setAvailable(pluginActive, null)
                    if (!pluginActive) toolWindow.hide(null)
                }
            }
        }
    }

    private fun refreshTopPanels() {
        ApplicationManager.getApplication().invokeLater {
            val topPanelService = project.service<TopPanelService>()
            val manager = FileEditorManager.getInstance(project)
            manager.allEditors.forEach(topPanelService::processEditor)
        }
    }

    private companion object {
        val TOOL_WINDOW_IDS = listOf(
            "NSMP SDK Sources Sync",
            "NSMP SDK Diff Manager",
        )
    }
}
