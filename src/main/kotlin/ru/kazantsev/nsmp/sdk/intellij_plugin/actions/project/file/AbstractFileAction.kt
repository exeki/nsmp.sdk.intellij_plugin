package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService

abstract class AbstractFileAction : AnAction() {

    abstract fun computableWithFile(project: Project, file: VirtualFile): Boolean

    @Suppress("unused")
    abstract fun internalPerform(event: AnActionEvent, project: Project, file : VirtualFile)

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val pluginActive = project.service<PluginActivationService>().pluginActive
        val dialogService = project.service<DialogNotificationService>()
        if (pluginActive) {
            val computableWithFile = computableWithFile(project, file)
            if (computableWithFile) internalPerform(event, project, file)
            else dialogService.showInfo( //TODO заменить на бандл
                "NSMP SDK Action error",
                "Current file ${file.name} is not computable with this action."
            )
        } else dialogService.showInfo( //TODO заменить на бандл
            "NSMP SDK Plugin is disabled",
            "Plugin is not active. Set the installation in the plugin settings to fix it."
        )
    }
}
