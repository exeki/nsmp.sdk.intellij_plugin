package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.UpdateRemoteStateBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

open class ProjectRemoteStateUpdateAction : AbstractProjectAction() {
    override val titleKey = "sync.command.remote.state.update.title"
    override val presentationTextKey = "action.project.remote.state.update.text"
    override val presentationDescriptionKey = "action.project.remote.state.update.description"

    override fun actionPerformedProject(
        event: AnActionEvent,
        project: Project
    ) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        if (backgroundTaskRegistry.getByType<UpdateRemoteStateBackgroundTask>().isNotEmpty()) {
            throw ActionStartException(MessageBundle.message("action.error.remote.state.task.running"))
        } else {
            UpdateRemoteStateBackgroundTask(project).start()
        }
    }
}
