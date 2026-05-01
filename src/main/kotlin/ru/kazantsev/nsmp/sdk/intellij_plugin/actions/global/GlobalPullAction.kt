package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.SrcOptionsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.tasks.backgroundable.PullBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.RemoteSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

class GlobalPullAction : AbstractGlobalSyncAction() {

    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return RemoteSrcOptionsProvider(project.service<SrcOptionsService>())
    }

    override fun getAction(project: Project): (SrcRequestSelectState) -> Unit {
        return { state: SrcRequestSelectState ->
            ProgressManager.getInstance().run(PullBackgroundTask(project = project, request = state.getRequest()))
        }
    }
}
