package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService

class FileExecuteButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.execute.short.title"),
    file = file,
    project = project
) {
    override fun compatibleWithFile(): Boolean {
        return file.extension.equals("groovy", ignoreCase = true)
    }

    private fun getText(file: VirtualFile): String {
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = fileDocumentManager.getDocument(file) ?: throw RuntimeException("Can't get document for file ${file.path}")
        fileDocumentManager.saveDocument(document)
        return document.text
    }

    override fun actionPerformed(event: AnActionEvent) {
        val projectSettingsService = project.service<ProjectSettingsService>()
        val dialogNotificationService = project.service<DialogNotificationService>()
        val connectionParams = projectSettingsService.connectorParams
        val connector = Connector(connectionParams)
        try {
            val result = connector.execFile(getText(file))
            dialogNotificationService.showInfo(
                title = MessageBundle.message("sync.command.execute.file.result.title"),
                message = result
            )
        } catch (e: Throwable) {
            if (e is BadResponseException) dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                message = e.responseSnapshot.bodyAsString
            )
            else dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                error = e
            )
        }
    }
}
