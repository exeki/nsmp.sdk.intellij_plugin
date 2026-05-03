package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.PushBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.common.FilePushConfirmationDialog

class FilePushAction : AbstractSrcSyncFileAction() {

    override val title: String = MessageBundle.message("sync.command.push.title")
    override fun actionPerformedSyncFile(
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    ) {
        val syncService = project.service<SyncService>()
        val confirmationDialog = FilePushConfirmationDialog(project)
        if (!confirmationDialog.showAndGet()) return
        val request = syncService.getSrcRequestForFileElseThrow(file)
        PushBackgroundTask(project, request, confirmationDialog.force).start()
    }


}
