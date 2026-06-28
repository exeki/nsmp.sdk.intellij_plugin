package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.sync.MetaClassSyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

/**
 * Фоновая задача полной синхронизации метаклассов с инсталляции.
 */
class MetaClassSyncBackgroundTask(
    project: Project,
) : AbstractBackgroundTask(
    MessageBundle.message("metaclass.sync.task.title"),
    project,
) {

    private val metaClassSyncService = project.service<MetaClassSyncService>()
    private var syncedCount: Int = 0

    override fun runInternal(indicator: ProgressIndicator) {
        indicator.text = MessageBundle.message("metaclass.sync.task.progress.fetch")
        indicator.fraction = 0.2
        val metaClasses = metaClassSyncService.syncDefaultBranches()
        syncedCount = metaClasses.size
        indicator.fraction = 1.0
    }

    override fun onSuccess() {
        project.service<BalloonNotificationService>().showInfo(
            title = taskTitle,
            content = MessageBundle.message("metaclass.sync.task.success", syncedCount),
        )
    }
}
