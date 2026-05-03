package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.FileExecuteBackgroundTask
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcFormat

class FileExecuteAction : AbstractFileAction() {
    override val title: String = "Execute file"

    override fun computableWithFile(project: Project, file: VirtualFile): Boolean {
        return file.exists() && file.extension == SrcFormat.GROOVY.code
    }

    override fun actionPerformedFile(
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    ) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        val hasStarted = backgroundTaskRegistry.getByType<FileExecuteBackgroundTask>().any { it.file.path == file.path }
        if (hasStarted) throw ActionStartException("Another execution task with current file is already started")
        val projectSettingsService = project.service<ProjectSettingsService>()
        val contextFileRelativePath = projectSettingsService.getExecutionContext(file.nameWithoutExtension)
        FileExecuteBackgroundTask(project, file, contextFileRelativePath).start()
    }
}
