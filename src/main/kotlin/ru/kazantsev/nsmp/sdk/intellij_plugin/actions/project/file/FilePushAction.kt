package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.PushBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.common.FilePushConfirmationDialog

class FilePushAction : AbstractSrcSyncFileAction() {

    override val titleKey = "sync.command.push.title"
    override val presentationTextKey = "action.file.push.text"
    override val presentationDescriptionKey = "action.file.push.description"
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
