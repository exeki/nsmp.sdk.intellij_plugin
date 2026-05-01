package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ScriptExecutionService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FileExecuteAction : AbstractGroovyFileAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val scriptExecutionService = project.service<ScriptExecutionService>()
        val projectSettingsService = project.service<ProjectSettingsService>()
        val dialogNotificationService = project.service<DialogNotificationService>()
        val contextFileRelativePath = projectSettingsService.getExecutionContext(file.nameWithoutExtension)
        val payload = try {
            scriptExecutionService.buildExecutionPayload(file, contextFileRelativePath)
        } catch (e: Throwable) {
            dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                error = e
            )
            return
        }
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            MessageBundle.message("sync.command.execute.file.result.title"),
            true
        ) {

            private var result: String? = null

            override fun run(indicator: ProgressIndicator) {
                result = scriptExecutionService.executePayload(payload)
            }

            override fun onSuccess() {
                dialogNotificationService.showInfo(
                    title = MessageBundle.message("sync.command.execute.file.result.title"),
                    message = result.orEmpty()
                )
            }

            override fun onThrowable(error: Throwable) {
                if (error is BadResponseException) dialogNotificationService.showError(
                    title = MessageBundle.message("sync.command.execute.file.error.title"),
                    message = error.responseSnapshot.bodyAsString
                )
                else dialogNotificationService.showError(
                    title = MessageBundle.message("sync.command.execute.file.error.title"),
                    error = error
                )
            }
        })
    }
}
