package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.SyncCheckBackgroundTask

class FileSyncCheckAction : AbstractSrcFileAction() {
    override fun internalPerform(
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    ) {
        val syncService = project.service<SyncService>()
        val request = syncService.getSrcRequestForFileElseThrow(file)
        ProgressManager.getInstance().run(
            SyncCheckBackgroundTask(
                project = project,
                request = request
            )
        )
    }
}
