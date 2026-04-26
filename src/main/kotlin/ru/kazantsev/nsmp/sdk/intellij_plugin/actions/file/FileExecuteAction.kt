package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ScriptExecutionService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
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
        try {
            val result = scriptExecutionService.executeFile(file, contextFileRelativePath)
            dialogNotificationService.showInfo(
                title = MessageBundle.message("sync.command.execute.file.result.title"),
                message = result
            )
        } catch (e: Throwable) {
            if (e is BadResponseException) dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                message = e.responseSnapshot.bodyAsString
            )
            else dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                error = e
            )
        }
    }
}