package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.AbstractProjectAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ActionStartException
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

abstract class AbstractFileAction : AbstractProjectAction() {

    abstract fun computableWithFile(project: Project, file: VirtualFile): Boolean

    override fun actionPerformedProject(event: AnActionEvent, project: Project) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: throw ActionStartException(MessageBundle.message("action.error.file.not.found"))
        val computableWithFile = computableWithFile(project, file)
        if (!computableWithFile) {
            throw ActionStartException(MessageBundle.message("action.error.file.not.computable", file.name))
        }
        actionPerformedFile(event, project, file)
    }

    abstract fun actionPerformedFile(event: AnActionEvent, project: Project, file: VirtualFile)
}
