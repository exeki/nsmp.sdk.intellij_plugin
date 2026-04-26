package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType

class FileOpenInBrowserAction() : AbstractSrcFileAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val projectSettingsService = project.service<ProjectSettingsService>()
        val syncService = project.service<SyncService>()
        val connectorParams = projectSettingsService.connectorParams
        val type = syncService.getSrcTypeElseThrow(file)
        val code = syncService.resolveSrcCode(file)
        val base = "${connectorParams.scheme}://${connectorParams.host}/sd/admin"
        val url = when (type) {
            SrcType.SCRIPT -> "$base/#script:$code"
            SrcType.MODULE -> "$base/#scriptModule:$code"
            SrcType.ADV_IMPORT -> "$base/#advImportConfig:$code"
        }
        BrowserUtil.browse(url)
    }

}
