package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.tasks.backgroundable.FileExecuteBackgroundTask

class FileExecuteAction : AbstractGroovyFileAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        if (!computableWithFile(project, file)) return
        val projectSettingsService = project.service<ProjectSettingsService>()
        val contextFileRelativePath = projectSettingsService.getExecutionContext(file.nameWithoutExtension)
        ProgressManager.getInstance().run(FileExecuteBackgroundTask(project, file, contextFileRelativePath))
    }
}
