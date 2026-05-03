package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSyncCheckPair

class SyncCheckBackgroundTask(
    project: Project,
    request: SrcRequest,
) : AbstractBackgroundSyncTask(
    MessageBundle.message("sync.command.sync.check.title"),
    project,
    request
) {

    private var result: SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>>? = null

    override fun runInternal(indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        ApplicationManager.getApplication().invokeAndWait {
            FileDocumentManager.getInstance().saveAllDocuments()
        }
        indicator.fraction = 0.2
        result = project.service<SyncService>().syncCheck(request)
        indicator.fraction = 1.0
    }

    override fun onSuccess() {
        val value = result ?: return
        val notificationService = project.service<BalloonNotificationService>()
        if (value.any { entry -> entry.value.any { it.conflict } }) notificationService.showError(
            title = taskTitle,
            content = MessageBundle.message("sync.command.sync.check.not.in.sync")
        )
        else notificationService.showInfo(
            title = taskTitle,
            content = MessageBundle.message("sync.command.sync.check.success")
        )
    }
}
