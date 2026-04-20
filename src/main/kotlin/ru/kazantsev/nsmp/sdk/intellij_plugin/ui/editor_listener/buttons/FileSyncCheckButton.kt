package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle

class FileSyncCheckButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.sync.check.short.title"),
    file = file,
    project = project
) {
    override fun actionPerformed(event: AnActionEvent) {
        syncUIAdapter.syncCheck(
            file = file,
            onSuccessCallback = { result ->
                if (result.isEmpty()) balloonNotificationService.showInfo(
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
