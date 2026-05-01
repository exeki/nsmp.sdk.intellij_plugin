package ru.kazantsev.nsmp.sdk.intellij_plugin.tasks.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ScriptExecutionService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FileExecuteBackgroundTask(
    project: Project,
    file: VirtualFile,
    contextFileRelativePath: String?,
) : Task.Backgroundable(
    project,
    "NSMP SDK File execute", //TODO на бандл
    true
) {
    private val scriptExecutionService = project.service<ScriptExecutionService>()
    val payload = scriptExecutionService.buildExecutionPayload(file, contextFileRelativePath)
    var result: String? = null

    override fun run(indicator: ProgressIndicator) {
        result = scriptExecutionService.executePayload(payload)
    }

    override fun onSuccess() {
        project.service<DialogNotificationService>().showInfo(
            title = MessageBundle.message("sync.command.execute.file.result.title"),
            message = result.orEmpty()
        )
    }

    override fun onThrowable(error: Throwable) {
        val dialogNotificationService = project.service<DialogNotificationService>()
        if (error is BadResponseException) {
            dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                message = error.responseSnapshot.bodyAsString
            )
        } else {
            dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                error = error
            )
        }
    }
}
