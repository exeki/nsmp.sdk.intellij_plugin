package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project

import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.SyncCheckBackgroundTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.LocalSrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider.SrcOptionsProvider

class ProjectSyncCheckAction : AbstractProjectSyncAction() {

    override val title: String = MessageBundle.message("sync.command.sync.check.title")

    override fun getOptionProvider(project: Project): SrcOptionsProvider {
        return LocalSrcOptionsProvider(project)
    }

    override fun getAction(project: Project) = { state: SrcRequestSelectState ->
        SyncCheckBackgroundTask(project, state.getRequest()).start()
    }
}
