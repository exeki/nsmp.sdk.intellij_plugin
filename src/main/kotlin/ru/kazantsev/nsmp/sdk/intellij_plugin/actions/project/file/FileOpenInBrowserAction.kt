package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType

class FileOpenInBrowserAction : AbstractSrcFileAction() {

    override val titleKey = "action.file.open.in.browser.title"
    override val presentationTextKey = "action.file.open.in.browser.text"
    override val presentationDescriptionKey = "action.file.open.in.browser.description"

    override fun actionPerformedFile(
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    ) {
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
