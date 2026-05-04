package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService

abstract class AbstractProjectAction : AnAction() {

    abstract val title: String

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: throw ActionStartException("Cant find project for action")
        try {
            val pluginActive = project.service<PluginActivationService>().pluginActive
            if (!pluginActive) throw ActionStartException("NSMP SDK Plugin is not active. Set the installation in the plugin settings to fix it.")
            actionPerformedProject(event, project)
        } catch (error: ActionStartException) {
            project.service<DialogNotificationService>().showError(title, error)
        }
    }

    abstract fun actionPerformedProject(event: AnActionEvent, project: Project)

}