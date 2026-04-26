package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FilePullAction() : AbstractSrcFileAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val syncUIAdapter = project.service<SyncUIAdapter>()
        val dialogNotificationService = project.service<DialogNotificationService>()
        val balloonNotificationService = project.service<BalloonNotificationService>()
        syncUIAdapter.pull(
            file = file,
            onSuccessCallback = {
                balloonNotificationService.showInfo(
                    MessageBundle.message("sync.command.pull.file.notification.title"),
                    MessageBundle.message("sync.command.pull.file.success")
                )
            },
            onFailureCallback = { error ->
                dialogNotificationService.showError(
                    title = MessageBundle.message("sync.command.pull.file.notification.title"),
                    message = MessageBundle.message("sync.command.error"),
                    error = error
                )
            }
        )
    }
}
