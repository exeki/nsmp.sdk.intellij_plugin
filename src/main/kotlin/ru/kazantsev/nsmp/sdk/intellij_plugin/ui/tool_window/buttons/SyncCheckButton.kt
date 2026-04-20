package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.LocalSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.SrcOptionsProvider
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot

class SyncCheckButton(
    project: Project
) : AbstractSyncButton(
    title = MessageBundle.message("sync.command.sync.check.short.title"),
    dialogTitle = MessageBundle.message("sync.command.sync.check.title"),
    project = project
) {
    override val optionsProvider: SrcOptionsProvider
        get() = LocalSrcOptionsProvider(project.service<ProjectSettingsService>())

    override val action: (SrcRequestSelectState) -> Unit
        get() = { state: SrcRequestSelectState ->
            syncUIAdapter.syncCheck(
                request = state.getRequest(),
                onSuccessCallback = { value: SrcInfoRoot ->
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
