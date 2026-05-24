package ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.LogicException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.NotificationUtils.Companion.buildErrorText
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.NotificationUtils.Companion.buildTitle

@Suppress("unused")
@Service(Service.Level.PROJECT)
class DialogNotificationService(private val project: Project) {
    fun showInfo(title: String, message: String) {
        Messages.showInfoMessage(message, buildTitle(title))
    }

    fun showInfo(
        title: String,
        message: String,
        actionTitle: String,
        onAction: () -> Unit
    ) {
        val result = Messages.showDialog(
            project,
            message,
            buildTitle(title),
            arrayOf(actionTitle, "Close"),
            1,
            Messages.getInformationIcon()
        )
        if (result == 0) onAction()
    }

    fun showError(title: String, message: String) {
        Messages.showErrorDialog(project, message, buildTitle(title))
    }

    fun showError(
        title: String,
        message: String,
        actionTitle: String,
        onAction: () -> Unit
    ) {
        val result = Messages.showDialog(
            project,
            message,
            buildTitle(title),
            arrayOf(actionTitle, "Close"),
            1,
            Messages.getErrorIcon()
        )
        if (result == 0) onAction()
    }

    fun showError(title: String, error: Throwable) {
        if (error is LogicException) showError(
            title = title,
            message = error.message
        )
        else Messages.showErrorDialog(project, buildErrorText(error), buildTitle(title))
    }

    fun showError(title: String, message: String, error: Throwable) {
        if (error is LogicException) showError(
            title = title,
            message = message + "\n" + error.message
        )
        else Messages.showErrorDialog(project, buildErrorText(message, error), buildTitle(title))
    }

}
