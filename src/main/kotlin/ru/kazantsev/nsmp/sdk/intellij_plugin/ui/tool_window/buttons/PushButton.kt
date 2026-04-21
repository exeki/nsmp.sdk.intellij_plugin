package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.LocalSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.SrcOptionsProvider
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.exception.SyncCheckFailedException

class PushButton(
    project: Project
) : AbstractSyncButton(
    title = MessageBundle.message("sync.command.push.short.title"),
    dialogTitle = MessageBundle.message("sync.command.push.title"),
    project = project
) {
    override val optionsProvider: SrcOptionsProvider
        get() = LocalSrcOptionsProvider(project.service<ProjectSettingsService>())

    override val action: (SrcRequestSelectState) -> Unit
        get() = { state: SrcRequestSelectState ->
            syncUIAdapter.push(
                request = state.getRequest(),
                force = state.force,
                onSuccessCallback = { value: SrcInfoRoot ->
                    balloonNotificationService.showInfo(
                        MessageBundle.message("sync.command.push.title"),
                        MessageBundle.message("sync.command.push.success", value.scripts.size, value.modules.size, value.advImports.size),
                    )
                },
                onFailureCallback = { e: Throwable ->
                    if (e is SyncCheckFailedException) {
                        dialogNotificationService.showInfo(
                            title = MessageBundle.message("sync.command.push.title"),
                            message = MessageBundle.message("sync.command.push.sync.check.failed")
                        )
                    } else {
                        dialogNotificationService.showError(
                            title = MessageBundle.message("sync.command.push.title"),
                            message = e.message ?: MessageBundle.message("sync.error.unknown")
                        )
                    }
                }
            )
        }
}
