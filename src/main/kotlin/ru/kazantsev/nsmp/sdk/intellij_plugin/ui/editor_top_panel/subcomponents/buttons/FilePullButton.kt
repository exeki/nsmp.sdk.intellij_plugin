@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file.FilePullAction

class FilePullButton(
    project: Project,
    file: VirtualFile,
) : AbstractButton(
    file = file,
    project = project,
    action = FilePullAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Pull"
        description = "Pull current file"
        isEnabledAndVisible = true
        icon = null
    }
)
