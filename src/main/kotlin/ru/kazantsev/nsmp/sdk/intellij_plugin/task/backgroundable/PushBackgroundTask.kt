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
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state.LastSyncedSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.PushSyncCheckFailedException

class PushBackgroundTask(
    project: Project,
    private val request: SrcRequest,
    private val force: Boolean,
) : Task.Backgroundable(project, MessageBundle.message("sync.command.push.title"), true) {

    private var result: SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>>? = null

    override fun run(indicator: ProgressIndicator) {
        val guardService = project.service<SyncTaskGroupGuardService>()
        guardService.acquireOrThrow()
        try {
            ApplicationManager.getApplication().invokeAndWait {
                FileDocumentManager.getInstance().saveAllDocuments()
            }
            val root = project.service<SyncService>().push(request, force)
            project.service<LastSyncedSrcStateService>().updateLastSyncedState(root.convert { _, pair -> pair.left })
            result = root
        } finally {
            guardService.release()
        }
    }

    override fun onSuccess() {
        val value = result ?: return
        project.service<BalloonNotificationService>().showInfo(
            title = MessageBundle.message("sync.command.push.title"),
            message = MessageBundle.message(
                "sync.command.push.success",
                value.scripts.size,
                value.modules.size,
                value.advImports.size
            )
        )
    }

    override fun onThrowable(error: Throwable) {
        val dialogService = project.service<DialogNotificationService>()
        when (error) {
            is SyncTaskAlreadyRunningException -> dialogService.showError(
                title = MessageBundle.message("sync.command.push.title"),
                message = error.message,
                error = error
            )

            is PushSyncCheckFailedException -> dialogService.showError(
                title = MessageBundle.message("sync.command.push.title"),
                message = MessageBundle.message("sync.command.push.sync.check.failed")
            )

            else -> dialogService.showError(
                title = MessageBundle.message("sync.command.push.title"),
                message = MessageBundle.message("sync.command.error"),
                error = error
            )
        }

    }

    override fun onFinished() {
        project.service<FileSupportService>().refreshProjectExplorer()
    }
}
