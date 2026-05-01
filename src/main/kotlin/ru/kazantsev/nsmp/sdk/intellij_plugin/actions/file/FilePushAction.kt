package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.tasks.backgroundable.PushBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class FilePushAction() : AbstractSrcFileAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val syncService = project.service<SyncService>()

        val confirmationDialog = PushConfirmationDialog(project)
        if (!confirmationDialog.showAndGet()) return

        val request = syncService.getSrcRequestForFileElseThrow(file)
        ProgressManager.getInstance().run(PushBackgroundTask(
            project = project,
            request = request,
            force = confirmationDialog.force,
        ))
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
