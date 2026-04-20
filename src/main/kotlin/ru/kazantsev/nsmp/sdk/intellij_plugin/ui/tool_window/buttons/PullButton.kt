package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.SrcOptionsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.RemoteSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.SrcOptionsProvider
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot

class PullButton(
    project: Project
) : AbstractSyncButton(
    title = MessageBundle.message("sync.command.pull.short.title"),
    dialogTitle = MessageBundle.message("sync.command.pull.title"),
    project = project
) {
    override val optionsProvider: SrcOptionsProvider
        get() = RemoteSrcOptionsProvider(project.service<SrcOptionsService>())

    override val action: (SrcRequestSelectState) -> Unit
        get() = { state: SrcRequestSelectState ->
            syncUIAdapter.pull(
                request = state.getRequest(),
                onSuccessCallback = { value: SrcDtoRoot ->
                    balloonNotificationService.showInfo(
                        MessageBundle.message("sync.command.pull.title"),
                        MessageBundle.message("sync.command.pull.success", value.scripts.size, value.modules.size, value.advImports.size),
                    )
                },
                onFailureCallback = { e: Throwable ->
                    balloonNotificationService.showError(
                        MessageBundle.message("sync.command.pull.title"),
                        e.message ?: MessageBundle.message("sync.error.unknown")
                    )
                }
            )
        }
}
