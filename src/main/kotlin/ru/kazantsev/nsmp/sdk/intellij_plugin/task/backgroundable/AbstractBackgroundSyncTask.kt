package ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.FileSupportService
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest

abstract class AbstractBackgroundSyncTask(
    title: String,
    project: Project,
    val request: SrcRequest
) : AbstractBackgroundTask(title, project) {

    override fun onFinished() {
        project.service<FileSupportService>().refreshProjectExplorerAsync()
    }

}