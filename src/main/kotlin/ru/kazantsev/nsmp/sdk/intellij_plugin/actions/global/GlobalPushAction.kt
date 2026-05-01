package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.LocalSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.PushSyncCheckFailedException

class GlobalPushAction : AbstractGlobalSyncAction() {
    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return LocalSrcOptionsProvider(project.service<ProjectSettingsService>())
    }

    override fun getAction(project: Project): (SrcRequestSelectState) -> Unit {
        val balloonNotificationService = project.service<BalloonNotificationService>()
        val dialogNotificationService = project.service<DialogNotificationService>()
        val syncService = project.service<SyncService>()
        return { state: SrcRequestSelectState ->
            syncService.push(
                request = state.getRequest(),
                force = state.force,
                onSuccessCallback = { value: SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>> ->
                    balloonNotificationService.showInfo(
                        MessageBundle.message("sync.command.push.title"),
                        MessageBundle.message(
                            "sync.command.push.success",
                            value.scripts.size,
                            value.modules.size,
                            value.advImports.size
                        ),
                    )
                },
                onFailureCallback = { e: Throwable ->
                    if (e is PushSyncCheckFailedException) dialogNotificationService.showInfo(
                        title = MessageBundle.message("sync.command.push.title"),
                        message = MessageBundle.message("sync.command.push.sync.check.failed")
                    )
                    else dialogNotificationService.showError(
                        title = MessageBundle.message("sync.command.push.title"),
                        message = e.message ?: MessageBundle.message("sync.error.unknown")
                    )
                }
            )
        }
    }
}