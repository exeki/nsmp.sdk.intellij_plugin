package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.SrcOptionsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.RemoteSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum

class GlobalPullAction : AbstractGlobalSyncAction() {

    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return RemoteSrcOptionsProvider(project.service<SrcOptionsService>())
    }

    override fun getAction(project: Project): (SrcRequestSelectState) -> Unit {
        val balloonNotificationService = project.service<BalloonNotificationService>()
        val syncService = project.service<SyncService>()
        return { state: SrcRequestSelectState ->
            syncService.pull(
                request = state.getRequest(),
                onSuccessCallback = { value: SrcSetRoot<SrcFileChecksum> ->
                    balloonNotificationService.showInfo(
                        title = MessageBundle.message("sync.command.pull.title"),
                        message = MessageBundle.message(
                            "sync.command.pull.success",
                            value.scripts.size,
                            value.modules.size,
                            value.advImports.size
                        ),
                    )
                },
                onFailureCallback = { e: Throwable ->
                    balloonNotificationService.showError(
                        title = MessageBundle.message("sync.command.pull.title"),
                        message = e.message ?: MessageBundle.message("sync.error.unknown")
                    )
                }
            )
        }
    }
}