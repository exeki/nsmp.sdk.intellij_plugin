package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.UpdateRemoteStateBackgroundTask

class ProjectRemoteStateUpdateAction : AbstractProjectAction() {
    override val title: String = "Update remote state" //TODO заменить на бандл

    override fun actionPerformedProject(
        event: AnActionEvent,
        project: Project
    ) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        if (backgroundTaskRegistry.getByType<UpdateRemoteStateBackgroundTask>().isNotEmpty()) {
            project.service<DialogNotificationService>().showError(

                title,
                "Another update remote state task is already running" //TODO заменить на бандл
            )
        } else UpdateRemoteStateBackgroundTask(project).start()
    }
}