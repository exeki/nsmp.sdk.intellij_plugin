package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SrcType

class FilePullButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.pull.short.title"),
    file = file,
    project = project
) {
    override fun actionPerformed(event: AnActionEvent) {
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

    override fun compatibleWithFile(): Boolean {
        return syncUIAdapter.getSrcType(file) in listOf(SrcType.SCRIPT, SrcType.MODULE, SrcType.ADV_IMPORT)
    }
}
