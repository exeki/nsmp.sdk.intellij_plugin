@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file.FileExecuteAction

class FileExecuteButton(
    project: Project,
    file: VirtualFile,
) : AbstractButton(
    file = file,
    project = project,
    action = FileExecuteAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Execute"
        description = "Send current file for execution"
        isEnabledAndVisible = true
        icon = null
    }
)
