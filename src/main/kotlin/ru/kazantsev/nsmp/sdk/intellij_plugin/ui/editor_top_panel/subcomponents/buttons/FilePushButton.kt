@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file.FilePushAction

class FilePushButton(
    project: Project,
    file: VirtualFile,
) : AbstractButton(
    file = file,
    project = project,
    action = FilePushAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Push"
        description = "Push current file"
        isEnabledAndVisible = true
        icon = null
    }
)
