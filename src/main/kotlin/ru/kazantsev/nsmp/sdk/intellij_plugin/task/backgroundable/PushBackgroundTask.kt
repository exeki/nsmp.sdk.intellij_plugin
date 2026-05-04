package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectRemoteStateUpdateAfterSyncCheckAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.state.LastSyncedSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.PushSyncCheckFailedException

class PushBackgroundTask(
    project: Project,
    request: SrcRequest,
    private val force: Boolean,
) : AbstractBackgroundSyncTask(
    MessageBundle.message("sync.command.push.title"),
    project,
    request
) {

    private var result: SrcSetRoot<SrcPair<SrcFileChecksum, SrcChecksum>>? = null

    override fun runInternal(indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        ApplicationManager.getApplication().invokeAndWait {
            FileDocumentManager.getInstance().saveAllDocuments()
        }
        indicator.fraction = 0.2
        val root = project.service<SyncService>().push(request, force)
        indicator.fraction = 0.7
        project.service<LastSyncedSrcStateService>().updateLastSyncedState(root.convert { _, pair -> pair.left })
        indicator.fraction = 1.0
        result = root
    }

    override fun onSuccess() {
        project.service<BalloonNotificationService>().showInfo(
            title = taskTitle,
            content = MessageBundle.message("sync.command.push.success")
        )
    }

    override fun onThrowable(error: Throwable) {
        val dialogService = project.service<DialogNotificationService>()

        if (error is PushSyncCheckFailedException) {
            val req = SrcSetRoot.byEnumIterator { type ->
                error.localSrcInfoRoot[type].filter { it.conflict }.toSet()
            }.convertToRequest()
            dialogService.showError(
                title = taskTitle,
                message = MessageBundle.message("sync.command.push.sync.check.failed"),
                actionTitle = "Show in diff manager",
                onAction = {
                    val event = AnActionEvent.createEvent(
                        SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
                        null,
                        ActionPlaces.UNKNOWN,
                        ActionUiKind.NONE,
                        null
                    )
                    ActionUtil.performAction(ProjectRemoteStateUpdateAfterSyncCheckAction(req), event)
                }
            )
        } else super.onThrowable(error)
    }
}
