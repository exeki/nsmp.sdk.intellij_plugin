package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state.LastSyncedSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum

class PullBackgroundTask(
    project: Project,
    request: SrcRequest,
) : AbstractBackgroundSyncTask(
    MessageBundle.message("sync.command.pull.title"),
    project,
    request
) {

    private var result: SrcSetRoot<SrcFileChecksum>? = null

    override fun runInternal(indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        ApplicationManager.getApplication().invokeAndWait {
            FileDocumentManager.getInstance().saveAllDocuments()
        }
        indicator.fraction = 0.2
        val root = project.service<SyncService>().pull(request)
        indicator.fraction = 0.7
        project.service<LastSyncedSrcStateService>().updateLastSyncedState(root)
        indicator.fraction = 1.0
        result = root
    }

    override fun onSuccess() {
        project.service<BalloonNotificationService>().showInfo(
            title = taskTitle,
            content = MessageBundle.message("sync.command.pull.success")
        )
    }
}
