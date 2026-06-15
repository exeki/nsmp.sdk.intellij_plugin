package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.AbstractBackgroundSyncTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.SrcRequestSelectDialog
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

abstract class AbstractProjectSyncAction : AbstractProjectAction() {
    fun getDialog(project: Project): DialogWrapper {
        return SrcRequestSelectDialog(
            title = title,
            project = project,
            withForceCheckbox = false,
            optionsProvider = getOptionProvider(project),
            action = getAction(project)
        )
    }

    override fun actionPerformedProject(event: AnActionEvent, project: Project) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        val hasStarted = backgroundTaskRegistry.getByType<AbstractBackgroundSyncTask>().isNotEmpty()
        if (hasStarted) throw ActionStartException(MessageBundle.message("action.error.sync.task.running"))
        val dialog = getDialog(project)
        dialog.showAndGet()
    }

    abstract fun getOptionProvider(project: Project): SrcOptionsProvider
    abstract fun getAction(project: Project): (SrcRequestSelectState) -> Unit
}