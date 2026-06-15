package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

abstract class AbstractProjectAction : AnAction() {

    abstract val titleKey: String
    abstract val presentationTextKey: String
    abstract val presentationDescriptionKey: String

    val title: String
        get() = MessageBundle.message(titleKey)

    override fun update(e: AnActionEvent) {
        e.presentation.text = MessageBundle.message(presentationTextKey)
        e.presentation.description = MessageBundle.message(presentationDescriptionKey)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
            ?: throw ActionStartException(MessageBundle.message("action.error.project.not.found"))
        try {
            val pluginActive = project.service<PluginActivationService>().pluginActive
            if (!pluginActive) {
                throw ActionStartException(MessageBundle.message("action.error.plugin.not.active"))
            }
            actionPerformedProject(event, project)
        } catch (error: ActionStartException) {
            project.service<DialogNotificationService>().showError(title, error)
        }
    }

    abstract fun actionPerformedProject(event: AnActionEvent, project: Project)
}
