package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FileSyncCheckAction() : AbstractSrcFileAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val syncUIAdapter = project.service<SyncUIAdapter>()
        val dialogNotificationService = project.service<DialogNotificationService>()
        val balloonNotificationService = project.service<BalloonNotificationService>()
        syncUIAdapter.syncCheck(
            file = file,
            onSuccessCallback = { result ->
                if (result.any { it.conflict }) balloonNotificationService.showInfo(
                    title = MessageBundle.message("sync.command.sync.check.file.notification.title"),
                    message = MessageBundle.message("sync.command.sync.check.file.success")
                )
                else balloonNotificationService.showError(
                    title = MessageBundle.message("sync.command.sync.check.file.notification.title"),
                    message = MessageBundle.message("sync.command.sync.check.file.not.in.sync")
                )
            },
            onFailureCallback = { error ->
                dialogNotificationService.showError(
                    title = MessageBundle.message("sync.command.sync.check.file.notification.title"),
                    message = MessageBundle.message("sync.command.error"),
                    error = error
                )
            }
        )
    }
}
