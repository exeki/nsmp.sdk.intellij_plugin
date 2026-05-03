package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService

abstract class AbstractProjectAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val pluginActive = project.service<PluginActivationService>().pluginActive
        if(pluginActive) internalPerform(event, project)
        else {
            val dialogService = project.service<DialogNotificationService>()
            //TODO заменить на бандл
            dialogService.showInfo(
                "NSMP SDK Plugin is disabled",
                "NSMP SDK Plugin is not active. Set the installation in the plugin settings to fix it."
            )
        }
    }

    @Suppress("unused")
    abstract fun internalPerform(event: AnActionEvent, project: Project)

}