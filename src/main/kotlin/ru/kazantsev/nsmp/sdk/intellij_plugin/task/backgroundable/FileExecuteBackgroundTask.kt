package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ScriptExecutionService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FileExecuteBackgroundTask(
    project: Project,
    val file: VirtualFile,
    val contextFileRelativePath: String?,
) : AbstractBackgroundTask(
    "File execute" + ": " + file.name,
    project
) {

    private val scriptExecutionService = project.service<ScriptExecutionService>()
    val payload = scriptExecutionService.buildExecutionPayload(file, contextFileRelativePath)
    var result: String? = null

    override fun runInternal(indicator: ProgressIndicator) {

        indicator.fraction = 0.5
        result = scriptExecutionService.executePayload(payload)
        indicator.fraction = 1.0
    }

    override fun onSuccess() {
        //TODO окошко бы побольше
        project.service<DialogNotificationService>().showInfo(
            title = "File execute",
            message = buildString {
                append("Executed file: ${file.name}")
                append("\n")
                if (!contextFileRelativePath.isNullOrBlank()) {
                    append(MessageBundle.message("sync.command.execute.context.title", contextFileRelativePath))
                    append("\n")
                }
                append(MessageBundle.message("sync.command.execute.file.result.title") + ":")
                append("\n")
                append("\n")
                append(result.orEmpty())
            }
        )
    }
}
