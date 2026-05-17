package ru.kazantsev.nsmp.sdk.intellij_plugin.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.service.NsdHttpServerService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.AppSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class NsdProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        logger.info("NSD Plugin startup activity triggered for project '${project.name}'")

        ApplicationManager.getApplication().getService(AppSettingsService::class.java)
        val serviceService = ApplicationManager.getApplication().getService(NsdHttpServerService::class.java)
        runCatching { serviceService.restart() }
            .onFailure {
                ApplicationManager.getApplication().invokeLater {
                    project.getService(BalloonNotificationService::class.java).showError(
                        MessageBundle.message("server.notification.title"),
                        MessageBundle.message("server.error.start.failed", it.message ?: "")
                    )
                }
            }.onSuccess { res: Boolean ->
                if (res) ApplicationManager.getApplication().invokeLater {
                    project.getService(BalloonNotificationService::class.java).showInfo(
                        MessageBundle.message("server.notification.title"),
                        MessageBundle.message("server.start.success")
                    )
                }
            }
        project.getService(PluginActivationService::class.java).refresh()

        logger.info("NSD Plugin services initialized for project '${project.name}'")
    }

    private companion object {
        private val logger = thisLogger()
    }
}
