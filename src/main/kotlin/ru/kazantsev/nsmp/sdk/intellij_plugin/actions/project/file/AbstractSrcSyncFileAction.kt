package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.BackgroundTaskRegistry
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.AbstractBackgroundSyncTask
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

abstract class AbstractSrcSyncFileAction : AbstractSrcFileAction() {

    override fun actionPerformedFile(
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    ) {
        val backgroundTaskRegistry = project.service<BackgroundTaskRegistry>()
        val hasStarted = backgroundTaskRegistry.getByType<AbstractBackgroundSyncTask>().isNotEmpty()
        if (hasStarted) throw ActionStartException(MessageBundle.message("action.error.sync.task.running"))
        actionPerformedSyncFile(event, project, file)
    }

    abstract fun actionPerformedSyncFile(
        @Suppress("unused")
        event: AnActionEvent,
        project: Project,
        file: VirtualFile
    )
}
