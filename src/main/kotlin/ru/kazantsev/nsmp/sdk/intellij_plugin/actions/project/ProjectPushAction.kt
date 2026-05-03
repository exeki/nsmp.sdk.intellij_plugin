package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.PushBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.LocalSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

class ProjectPushAction : AbstractProjectSyncAction() {
    override fun getDialogTitle(): String {
        return MessageBundle.message("sync.command.push.title")
    }

    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return LocalSrcOptionsProvider(project)
    }

    override fun getAction(project: Project): (SrcRequestSelectState) -> Unit {
        return { state: SrcRequestSelectState ->
            ProgressManager.getInstance().run(
                PushBackgroundTask(
                    project = project,
                    request = state.getRequest(),
                    force = state.force,
                )
            )
        }
    }
}
