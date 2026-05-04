package ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.LogicException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.NotificationUtils.Companion.buildTitle
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.NotificationUtils.Companion.buildErrorText

@Suppress("unused")
@Service(Service.Level.PROJECT)
class BalloonNotificationService(private val project: Project) {

    private val notifications by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("NSMP SDK Notifications")
    }

    fun showInfo(title: String, content: String) {
        notify(
            title = buildTitle(title),
            content = content,
            type = NotificationType.INFORMATION
        )
    }

    fun showInfo(
        title: String,
        content: String,
        actionTitle: String,
        action: () -> Unit
    ) {
        notifications.createNotification(
            title = buildTitle(title),
            content = content,
            type = NotificationType.INFORMATION
        ).addAction(NotificationAction.createSimple(actionTitle, action)).notify(project)
    }

    fun showError(
        title: String,
        content: String,
        actionTitle: String,
        action: () -> Unit
    ) {
        notifications.createNotification(
            title = buildTitle(title),
            content = content,
            type = NotificationType.ERROR
        ).addAction(NotificationAction.createSimple(actionTitle) {action()}).notify(project)
    }

    fun showError(title: String, content: String) = notify(
        title = buildTitle(title),
        content = content,
        type = NotificationType.ERROR
    )

    fun showError(title: String, e: Throwable) {
        if (e is LogicException) showError(
            title = title,
            content = e.message
        )
        else notify(
            title = buildTitle(title),
            content = buildErrorText(e),
            type = NotificationType.ERROR
        )
    }

    fun showError(title: String, content: String, e: Throwable) {
        if (e is LogicException) showError(
            title = title,
            content = content + "\n" + e.message
        )
        else notify(
            title = buildTitle(title),
            content = buildErrorText(content, e),
            type = NotificationType.ERROR
        )
    }

    private fun notify(
        title: String,
        content: String,
        type: NotificationType
    ) {
        notifications.createNotification(
            title = title,
            content = content,
            type = type
        ).notify(project)
    }
}
