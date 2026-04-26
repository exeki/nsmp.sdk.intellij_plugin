package ru.kazantsev.nsmp.sdk.intellij_plugin.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import ru.kazantsev.nsmp.sdk.intellij_plugin.listeners.source_roots.SourceRootsImportFinishedListener
import ru.kazantsev.nsmp.sdk.intellij_plugin.listeners.source_roots.SourceRootsVfsChangesListener
import ru.kazantsev.nsmp.sdk.intellij_plugin.listeners.top_panel.TopPanelVfsChangesListener

class ListenersStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<TopPanelVfsChangesListener>()
        project.service<SourceRootsVfsChangesListener>()
        project.service<SourceRootsImportFinishedListener>()
    }
}
