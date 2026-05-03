package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.SyncTaskAlreadyRunningException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncTaskGroupGuardService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSyncCheckPair

class SyncCheckBackgroundTask(
    project: Project,
    private val request: SrcRequest,
) : Task.Backgroundable(project, MessageBundle.message("sync.command.sync.check.title"), true) {

    private var result: SrcSetRoot<SrcSyncCheckPair<SrcFileChecksum, SrcChecksum>>? = null

    override fun run(indicator: ProgressIndicator) {
        val guardService = project.service<SyncTaskGroupGuardService>()
        guardService.acquireOrThrow()
        try {
            ApplicationManager.getApplication().invokeAndWait {
                FileDocumentManager.getInstance().saveAllDocuments()
            }
            result = project.service<SyncService>().syncCheck(request)
        } finally {
            guardService.release()
        }
    }

    override fun onSuccess() {
        val value = result ?: return
        val balloonNotificationService = project.service<BalloonNotificationService>()
        if (value.any { entry -> entry.value.any { it.conflict } }) {
            balloonNotificationService.showError(
                title = MessageBundle.message("sync.command.sync.check.title"),
                message = MessageBundle.message("sync.command.sync.check.not.in.sync")
            )
            return
        }
        balloonNotificationService.showInfo(
            title = MessageBundle.message("sync.command.sync.check.title"),
            message = MessageBundle.message(
                "sync.command.sync.check.success",
                value.scripts.size,
                value.modules.size,
                value.advImports.size
            )
        )
    }

    override fun onThrowable(error: Throwable) {
        val dialogService = project.service<DialogNotificationService>()
        if (error is SyncTaskAlreadyRunningException) dialogService.showError(
            title = MessageBundle.message("sync.command.pull.title"),
            message = error.message,
            error = error
        )
        else dialogService.showError(
            title = MessageBundle.message("sync.command.sync.check.title"),
            message = MessageBundle.message("sync.command.error"),
            error = error
        )
    }

    override fun onFinished() {
        project.service<FileSupportService>().refreshProjectExplorer()
    }
}
