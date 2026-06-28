package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.MetaClassSyncBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

/**
 * Action запуска синхронизации метаклассов и перегенерации IDE-заглушек.
 */
class ProjectMetaClassSyncAction : AbstractProjectAction() {

    override val titleKey = "metaclass.sync.title"
    override val presentationTextKey = "action.project.metaclass.sync.text"
    override val presentationDescriptionKey = "action.project.metaclass.sync.description"

    override fun actionPerformedProject(event: AnActionEvent, project: Project) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        if (backgroundTaskRegistry.getByType<MetaClassSyncBackgroundTask>().isNotEmpty()) {
            throw ActionStartException(MessageBundle.message("action.error.metaclass.sync.task.running"))
        }
        MetaClassSyncBackgroundTask(project).start()
    }
}
