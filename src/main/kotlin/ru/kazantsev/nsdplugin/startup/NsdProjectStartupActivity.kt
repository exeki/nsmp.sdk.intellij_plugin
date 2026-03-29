package ru.kazantsev.nsdplugin.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import ru.kazantsev.nsdplugin.server.service.NsdHttpServerService
import ru.kazantsev.nsdplugin.settings.NsdPluginSettingsService

class NsdProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        logger.info("NSD Plugin startup activity triggered for project '${project.name}'")

        ApplicationManager.getApplication().getService(NsdPluginSettingsService::class.java)
        ApplicationManager.getApplication().getService(NsdHttpServerService::class.java)

        logger.info("NSD Plugin services initialized for project '${project.name}'")
    }

    private companion object {
        private val logger = thisLogger()
    }
}
