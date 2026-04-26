package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.PushSyncCheckFailedException
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class FilePushAction() : AbstractSrcFileAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val syncUIAdapter = project.service<SyncUIAdapter>()
        val dialogNotificationService = project.service<DialogNotificationService>()
        val balloonNotificationService = project.service<BalloonNotificationService>()

        val confirmationDialog = PushConfirmationDialog(project)
        if (!confirmationDialog.showAndGet()) return

        syncUIAdapter.push(
            file = file,
            force = confirmationDialog.force,
            onSuccessCallback = {
                balloonNotificationService.showInfo(
                    MessageBundle.message("sync.command.push.file.notification.title"),
                    MessageBundle.message("sync.command.push.file.success")
                )
            },
            onFailureCallback = { error ->
                if (error is PushSyncCheckFailedException) dialogNotificationService.showError(
                    title = MessageBundle.message("sync.command.push.file.notification.title"),
                    message = MessageBundle.message("sync.command.push.sync.check.failed")
                )
                else dialogNotificationService.showError(
                    title = MessageBundle.message("sync.command.push.file.notification.title"),
                    message = MessageBundle.message("sync.command.error"),
                    error = error
                )
            }
        )
    }

    private class PushConfirmationDialog(project: Project) : DialogWrapper(project) {
        private val forceCheckBox = JBCheckBox(MessageBundle.message("sync.dialog.force"))

        val force: Boolean
            get() = forceCheckBox.isSelected

        init {
            title = MessageBundle.message("sync.command.push.file.confirmation.title")
            init()
        }

        override fun createCenterPanel(): JComponent {
            return JPanel(BorderLayout(0, 8)).apply {
                add(JBLabel(MessageBundle.message("sync.command.push.file.confirmation.question")), BorderLayout.NORTH)
                add(forceCheckBox, BorderLayout.CENTER)
            }
        }
    }
}
