package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ScriptExecutionService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.common.FileExecuteResultDialog

class FileExecuteBackgroundTask(
    project: Project,
    val file: VirtualFile,
    val contextFileRelativePath: String?,
) : AbstractBackgroundTask(
    MessageBundle.message("sync.command.execute.file.task.title", file.name),
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
        FileExecuteResultDialog.show(
            project = project,
            fileName = file.name,
            contextFileRelativePath = contextFileRelativePath,
            result = result.orEmpty(),
        )
    }
}
