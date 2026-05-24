package ru.kazantsev.nsmp.sdk.intellij_plugin.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import ru.kazantsev.nsmp.sdk.intellij_plugin.listener.service_dir.ServiceDirVfsChangesListener
import ru.kazantsev.nsmp.sdk.intellij_plugin.listener.top_panel.TopPanelVfsChangesListener
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.service.NsdHttpServerService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService

class PluginStartup : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<TopPanelVfsChangesListener>()
        project.service<ServiceDirVfsChangesListener>()
        project.service<PluginActivationService>().refresh()
        ApplicationManager.getApplication().service<NsdHttpServerService>().start()
    }
}