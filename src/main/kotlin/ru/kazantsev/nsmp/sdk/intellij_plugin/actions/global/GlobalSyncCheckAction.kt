package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.tasks.backgroundable.SyncCheckBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.LocalSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

class GlobalSyncCheckAction : AbstractGlobalSyncAction() {
    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return LocalSrcOptionsProvider(project.service<ProjectSettingsService>())
    }

    override fun getAction(project: Project) = { state: SrcRequestSelectState ->
        ProgressManager.getInstance().run(SyncCheckBackgroundTask(project = project, request = state.getRequest()))
    }
}
