package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.sources_sync.exception.SyncCheckFailedException
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class FilePushButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.push.short.title"),
    file = file,
    project = project
) {
    private var forceCheckBox: JCheckBox = JCheckBox(MessageBundle.message("sync.dialog.force")).also {
        it.alignmentY = Component.CENTER_ALIGNMENT
        it.isOpaque = false
        it.border = JBUI.Borders.empty()
        it.margin = JBUI.emptyInsets()
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val pushButton = createButtonComponent(presentation, place)
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            pushButton.alignmentY = Component.CENTER_ALIGNMENT
            add(pushButton)
            add(forceCheckBox)
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        syncUIAdapter.push(
            file = file,
            force = forceCheckBox.isSelected,
            onSuccessCallback = {
                balloonNotificationService.showInfo(
                    MessageBundle.message("sync.command.push.file.notification.title"),
                    MessageBundle.message("sync.command.push.file.success")
                )
            },
            onFailureCallback = { error ->
                if (error is SyncCheckFailedException) dialogNotificationService.showError(
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
        forceCheckBox.isSelected = false
    }
}
