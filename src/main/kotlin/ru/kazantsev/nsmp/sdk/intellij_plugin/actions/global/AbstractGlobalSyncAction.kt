package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.SrcRequestSelectDialog
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

abstract class AbstractGlobalSyncAction : AbstractGlobalAction() {
    fun getDialog(project: Project): DialogWrapper {
        return SrcRequestSelectDialog(
            title = MessageBundle.message("sync.command.pull.title"),
            project = project,
            withForceCheckbox = false,
            optionsProvider = getOptionProvider(project),
            action = getAction(project)
        )
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        val dialog = getDialog(project)
        dialog.showAndGet()
    }

    abstract fun getOptionProvider(project: Project): SrcOptionsProvider
    abstract fun getAction(project: Project): (SrcRequestSelectState) -> Unit
}