package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.FileExecuteBackgroundTask

class FileExecuteAction : AbstractGroovyFileAction() {

    override fun internalPerform(
        event: AnActionEvent,
        project: Project,
        file : VirtualFile
    ) {
        val projectSettingsService = project.service<ProjectSettingsService>()
        val contextFileRelativePath = projectSettingsService.getExecutionContext(file.nameWithoutExtension)
        ProgressManager.getInstance().run(FileExecuteBackgroundTask(project, file, contextFileRelativePath))
    }
}
