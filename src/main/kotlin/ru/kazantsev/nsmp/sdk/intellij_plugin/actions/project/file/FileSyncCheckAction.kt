package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.SyncCheckBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FileSyncCheckAction : AbstractSrcSyncFileAction() {

    override val title: String = MessageBundle.message("sync.command.sync.check.title")

    override fun actionPerformedSyncFile(
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    ) {
        val syncService = project.service<SyncService>()
        val request = syncService.getSrcRequestForFileElseThrow(file)
        SyncCheckBackgroundTask(project, request).start()
    }
}
