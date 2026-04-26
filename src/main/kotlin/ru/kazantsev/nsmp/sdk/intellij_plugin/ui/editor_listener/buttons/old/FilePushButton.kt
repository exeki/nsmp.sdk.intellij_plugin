package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.old

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.exception.commands.PushSyncCheckFailedException
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

@Deprecated("old")
class FilePushButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.push.short.title"),
    file = file,
    project = project
) {

    override fun actionPerformed(event: AnActionEvent) {
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
