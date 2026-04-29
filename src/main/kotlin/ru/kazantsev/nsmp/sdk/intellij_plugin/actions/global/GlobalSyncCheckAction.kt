package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.LocalSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.remote.RemoteInfo

class GlobalSyncCheckAction : AbstractGlobalSyncAction() {
    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return LocalSrcOptionsProvider(project.service<ProjectSettingsService>())
    }

    override fun getAction(project: Project) = { state: SrcRequestSelectState ->
        val balloonNotificationService = project.service<BalloonNotificationService>()
        val dialogNotificationService = project.service<DialogNotificationService>()
        val syncService = project.service<SyncService>()
        syncService.syncCheck(
            request = state.getRequest(),
            onSuccessCallback = { value: SrcSetRoot<SrcSyncCheckPair<LocalFileInfo, RemoteInfo>> ->
                if (value.isEmpty()) {
                    balloonNotificationService.showInfo(
                        title = MessageBundle.message("sync.command.sync.check.title"),
                        message = MessageBundle.message("sync.command.sync.check.no.changes")
                    )
                } else {
                    dialogNotificationService.showInfo(
                        title = MessageBundle.message("sync.command.sync.check.title"),
                        message = MessageBundle.message("sync.command.sync.check.success", value.scripts.size, value.modules.size, value.advImports.size)
                    )
                }
            },
            onFailureCallback = { e: Throwable ->
                dialogNotificationService.showError(
                    title = MessageBundle.message("sync.command.sync.check.title"),
                    message = e.message ?: MessageBundle.message("sync.error.unknown")
                )
            }
        )
    }
}