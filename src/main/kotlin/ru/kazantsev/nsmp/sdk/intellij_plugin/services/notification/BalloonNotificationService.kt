package ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

@Service(Service.Level.PROJECT)
class BalloonNotificationService(private val project: Project) {
    private val notifications by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("NSMP SDK Notifications")
    }

    fun showInfo(title: String, message: String) {
        notifications.createNotification(
            title = title,
            content = message,
            type = NotificationType.INFORMATION
        ).notify(project)
    }

    fun showError(title: String, message: String) {
        notifications.createNotification(
            title = title,
            content = message,
            type = NotificationType.ERROR
        ).notify(project)
    }

    fun showError(title: String, e: Throwable) {
        notifications.createNotification(
            title = title,
            content = NotificationErrorBuilder.buildErrorText(e),
            type = NotificationType.ERROR
        ).notify(project)
    }

    fun showError(title: String, message: String, e: Throwable) {
        notifications.createNotification(
            title = title,
            content = NotificationErrorBuilder.buildErrorText(message, e),
            type = NotificationType.ERROR
        ).notify(project)
    }
}
