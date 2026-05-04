package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.UpdateRemoteStateBackgroundTask

open class ProjectRemoteStateUpdateAction() : AbstractProjectAction() {
    override val title: String = "Update remote state" //TODO заменить на бандл

    override fun actionPerformedProject(
        event: AnActionEvent,
        project: Project
    ) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        if (backgroundTaskRegistry.getByType<UpdateRemoteStateBackgroundTask>().isNotEmpty()) {
            throw ActionStartException("Another update remote state task is already running")
        } else UpdateRemoteStateBackgroundTask(project).start()
    }
}