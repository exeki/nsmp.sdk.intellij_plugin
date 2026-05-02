package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncTaskGroupGuardService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state.LastSyncedSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum

class PullBackgroundTask(
    project: Project,
    private val request: SrcRequest,
) : Task.Backgroundable(project, MessageBundle.message("sync.command.pull.title"), true) {

    private var result: SrcSetRoot<SrcFileChecksum>? = null

    override fun run(indicator: ProgressIndicator) {
        val guardService = project.service<SyncTaskGroupGuardService>()
        guardService.acquireOrThrow()
        try {
            ApplicationManager.getApplication().invokeAndWait {
                FileDocumentManager.getInstance().saveAllDocuments()
            }
            val root = project.service<SyncService>().pull(request)
            project.service<LastSyncedSrcStateService>().updateLastSyncedState(root)
            result =root
        } finally {
            guardService.release()
        }
    }

    override fun onSuccess() {
        val value = result ?: return
        project.service<BalloonNotificationService>().showInfo(
            title = MessageBundle.message("sync.command.pull.title"),
            message = MessageBundle.message(
                "sync.command.pull.success",
                value.scripts.size,
                value.modules.size,
                value.advImports.size
            )
        )
    }

    override fun onThrowable(error: Throwable) {
        project.service<DialogNotificationService>().showError(
            title = MessageBundle.message("sync.command.pull.title"),
            message = MessageBundle.message("sync.command.error"),
            error = error
        )
    }

    override fun onFinished() {
        project.service<FileSupportService>().refreshProjectExplorer()
    }
}
