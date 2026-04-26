package ru.kazantsev.nsmp.sdk.intellij_plugin.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import ru.kazantsev.nsmp.sdk.intellij_plugin.listeners.TopPanelVfsChangesListener

class ListenersStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<TopPanelVfsChangesListener>()
    }

    private companion object {
        private val logger = thisLogger()
    }
}
