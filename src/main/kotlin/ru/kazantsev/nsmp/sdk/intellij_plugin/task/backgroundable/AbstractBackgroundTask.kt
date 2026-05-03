package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.basic_api_connector.exception.RequestProcessException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.util.*

abstract class AbstractBackgroundTask(
    val taskTitle: String,
    project: Project
) : Task.Backgroundable(
    project,
    "${MessageBundle.message("settings.display.name")}: $taskTitle",
    false
) {

    val registry = project.service<BackgroundTaskRegistry>()

    val uuid: UUID = UUID.randomUUID()

    var error: Throwable? = null

    fun start() = ProgressManager.getInstance().run(this)

    override fun run(indicator: ProgressIndicator) {
        registry.register(this)
        try {
            runInternal(indicator)
        } catch (error: Throwable) {
            this.error = error
            throw error
        } finally {
            registry.unregister(this)
        }
    }

    abstract fun runInternal(indicator: ProgressIndicator)

    override fun onThrowable(error: Throwable) {
        val dialogNotificationService = project.service<DialogNotificationService>()
        when (error) {
            is BadResponseException -> dialogNotificationService.showError(
                title = taskTitle,
                message = "Bad response from server: ${error.responseSnapshot.bodyAsString}"
            )

            is RequestProcessException -> dialogNotificationService.showError(
                title = taskTitle,
                message = error.cause?.javaClass?.name.orEmpty() + ": " + error.cause?.message.orEmpty()
            )

            else -> dialogNotificationService.showError(
                title = taskTitle,
                message = "Failed to execute background task:",
                error = error
            )
        }
    }
}
