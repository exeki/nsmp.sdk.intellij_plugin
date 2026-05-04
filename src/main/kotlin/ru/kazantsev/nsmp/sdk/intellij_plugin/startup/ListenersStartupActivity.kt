package ru.kazantsev.nsmp.sdk.intellij_plugin.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import ru.kazantsev.nsmp.sdk.intellij_plugin.listener.service_dir.ServiceDirVfsChangesListener
import ru.kazantsev.nsmp.sdk.intellij_plugin.listener.top_panel.TopPanelVfsChangesListener

class ListenersStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<TopPanelVfsChangesListener>()
        project.service<ServiceDirVfsChangesListener>()
    }
}