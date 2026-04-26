@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file.FileSyncCheckAction

class FileSyncCheckButton(
    project: Project,
    file: VirtualFile,
) : AbstractButton(
    file = file,
    project = project,
    action = FileSyncCheckAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Sync Check"
        description = "Sync check current file"
        isEnabledAndVisible = true
        icon = null
    }
)
