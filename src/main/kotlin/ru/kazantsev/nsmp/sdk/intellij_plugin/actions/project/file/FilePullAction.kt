package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.PullBackgroundTask

class FilePullAction : AbstractSrcSyncFileAction() {
    override val titleKey = "sync.command.pull.title"
    override val presentationTextKey = "action.file.pull.text"
    override val presentationDescriptionKey = "action.file.pull.description"

    override fun actionPerformedSyncFile(
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    ) {
        val syncService = project.service<SyncService>()
        val request = syncService.getSrcRequestForFileElseThrow(file)
        PullBackgroundTask(project, request).start()
    }
}