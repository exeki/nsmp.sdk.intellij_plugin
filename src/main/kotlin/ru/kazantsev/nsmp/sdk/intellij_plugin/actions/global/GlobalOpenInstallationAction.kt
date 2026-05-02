package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

class GlobalOpenInstallationAction : AbstractGlobalAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        val projectSettingsService = project.service<ProjectSettingsService>()
        val connectorParams = projectSettingsService.connectorParams
        val url = "${connectorParams.scheme}://${connectorParams.host}/sd/admin"
        BrowserUtil.browse(url)
    }
}