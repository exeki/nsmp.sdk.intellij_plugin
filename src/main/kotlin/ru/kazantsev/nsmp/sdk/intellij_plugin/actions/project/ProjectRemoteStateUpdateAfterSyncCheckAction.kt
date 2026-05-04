package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.UpdateRemoteStateBackgroundTask
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest

class ProjectRemoteStateUpdateAfterSyncCheckAction(val req: SrcRequest) : ProjectRemoteStateUpdateAction() {

    override fun actionPerformedProject(
        event: AnActionEvent,
        project: Project
    ) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        if (backgroundTaskRegistry.getByType<UpdateRemoteStateBackgroundTask>().isNotEmpty()) {
            throw ActionStartException("Another update remote state task is already running")
        } else UpdateRemoteStateBackgroundTask(project, true).start()
    }
}