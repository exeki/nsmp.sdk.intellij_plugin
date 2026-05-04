package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state.RemoteSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest

open class UpdateRemoteStateBackgroundTask(
    project: Project,
    val showLastSyncToRemoteInDiffManager: Boolean = false
) : AbstractBackgroundTask(
    //TODO перевести на бандл
    "Update remote state",
    project
) {

    override fun runInternal(indicator: ProgressIndicator) {
        indicator.fraction = 0.2
        val req = SrcRequest(allScripts = true, allAdvImports = true, allModules = true)
        project.service<RemoteSrcStateService>().updateRemoteState(req)
        indicator.fraction = 1.0
    }

    override fun onSuccess() {
        project.service<BalloonNotificationService>().showInfo(
            title = taskTitle,
            content = "Remote state update completed successfully" //TODO перевести на бандл
        )
    }
}
