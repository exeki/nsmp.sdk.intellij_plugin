package ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

@Suppress("unused")
@Service(Service.Level.PROJECT)
class DialogNotificationService(private val project: Project) {
    fun showInfo(title: String, message: String) {
        Messages.showInfoMessage(project, message, title)
    }

    fun showError(title: String, message: String) {
        Messages.showErrorDialog(project, message, title)
    }

    fun showError(title: String, message: String, e: Throwable) {
        Messages.showErrorDialog(project, NotificationErrorBuilder.buildErrorText(message, e), title)
    }

    fun showError(title: String, e: Throwable) {
        Messages.showErrorDialog(project, NotificationErrorBuilder.buildErrorText(e), title)
    }

}
