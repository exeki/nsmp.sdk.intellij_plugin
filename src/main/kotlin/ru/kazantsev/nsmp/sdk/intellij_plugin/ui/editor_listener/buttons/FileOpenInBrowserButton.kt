package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SrcType
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService

class FileOpenInBrowserButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.open.browser.short.title"),
    file = file,
    project = project
) {
    override fun actionPerformed(event: AnActionEvent) {
        val url = buildBrowserUrl(file)
        BrowserUtil.browse(url)
    }

    private fun buildBrowserUrl(file: VirtualFile): String {
        val projectSettingsService = project.service<ProjectSettingsService>()
        val syncService = project.service<SyncService>()
        val connectorParams = projectSettingsService.connectorParams
        val type = syncService.getSrcTypeElseThrow(file)
        val code = syncService.resolveSrcCode(file)
        val base = "${connectorParams.scheme}://${connectorParams.host}/sd/admin"
        return when (type) {
            SrcType.SCRIPT -> "$base/#script:$code"
            SrcType.MODULE -> "$base/#scriptModule:$code"
            SrcType.ADV_IMPORT -> "$base/#advImportConfig:$code"
        }
    }

    override fun compatibleWithFile(): Boolean {
        return syncUIAdapter.getSrcType(file) in listOf(SrcType.SCRIPT, SrcType.MODULE, SrcType.ADV_IMPORT)
    }
}
