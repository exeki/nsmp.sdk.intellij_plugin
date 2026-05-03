package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.SrcRequestSelectDialog
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

abstract class AbstractProjectSyncAction : AbstractProjectAction() {
    fun getDialog(project: Project): DialogWrapper {
        return SrcRequestSelectDialog(
            title = getDialogTitle(),
            project = project,
            withForceCheckbox = false,
            optionsProvider = getOptionProvider(project),
            action = getAction(project)
        )
    }

    override fun internalPerform(event: AnActionEvent, project: Project) {
        val dialog = getDialog(project)
        dialog.showAndGet()
    }

    abstract fun getDialogTitle(): String
    abstract fun getOptionProvider(project: Project): SrcOptionsProvider
    abstract fun getAction(project: Project): (SrcRequestSelectState) -> Unit
}