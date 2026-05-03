package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options.SrcOptionsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.PullBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.RemoteSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

class ProjectPullAction : AbstractProjectSyncAction() {
    override fun getDialogTitle(): String {
        return MessageBundle.message("sync.command.pull.title")
    }

    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return RemoteSrcOptionsProvider(project.service<SrcOptionsService>())
    }

    override fun getAction(project: Project): (SrcRequestSelectState) -> Unit {
        return { state: SrcRequestSelectState ->
            ProgressManager.getInstance().run(
                PullBackgroundTask(
                    project = project,
                    request = state.getRequest()
                )
            )
        }
    }
}
