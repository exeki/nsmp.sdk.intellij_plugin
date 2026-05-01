package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.tasks.backgroundable.PullBackgroundTask

class FilePullAction : AbstractSrcFileAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val syncService = project.service<SyncService>()
        val request = syncService.getSrcRequestForFileElseThrow(file)
        ProgressManager.getInstance().run(PullBackgroundTask(project = project, request = request))
    }
}
