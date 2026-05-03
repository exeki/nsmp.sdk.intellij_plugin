package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

class ProjectOpenInstallationAction : AbstractProjectAction() {
    override fun internalPerform(
        event: AnActionEvent,
        project: Project
    ) {
        val projectSettingsService = project.service<ProjectSettingsService>()
        val connectorParams = projectSettingsService.connectorParams
        val url = "${connectorParams.scheme}://${connectorParams.host}/sd/admin"
        BrowserUtil.browse(url)
    }
}