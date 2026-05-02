package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state.RemoteSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest

class UpdateRemoteStateBackgroundTask(
    project: Project,
    private val onCompleted: () -> Unit,
) : Task.Backgroundable(project, "NSMP SDK Update remote src state", true) {

    override fun run(indicator: ProgressIndicator) {
        val req = SrcRequest(allScripts = true, allAdvImports = true, allModules = true)
        project.service<RemoteSrcStateService>().updateRemoteState(req)
    }

    override fun onSuccess() {
        onCompleted()
    }

    override fun onThrowable(error: Throwable) {
        project.service<DialogNotificationService>().showError(
            title = "NSMP SDK Update remote src state",
            message = "Failed to update remote source state.",
            error = error
        )
    }
}
