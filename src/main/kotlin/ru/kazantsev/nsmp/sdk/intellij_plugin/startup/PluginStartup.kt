package ru.kazantsev.nsmp.sdk.intellij_plugin.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import ru.kazantsev.nsmp.sdk.intellij_plugin.listener.service_dir.ServiceDirVfsChangesListener
import ru.kazantsev.nsmp.sdk.intellij_plugin.listener.top_panel.TopPanelVfsChangesListener
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.service.NsdHttpServerService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ServiceDirSetupService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding.MetaClassBindingInvalidationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.sync.MetaClassSyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService

class PluginStartup : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<TopPanelVfsChangesListener>()
        project.service<ServiceDirVfsChangesListener>()
        project.service<MetaClassBindingInvalidationService>()
        project.service<PluginActivationService>().refresh()
        if (project.service<PluginActivationService>().pluginActive) {
            project.service<ServiceDirSetupService>().ensureServiceDirLayout()
            project.service<MetaClassSyncService>().regenerateFromStorage()
        }
        ApplicationManager.getApplication().service<NsdHttpServerService>().start()
    }
}