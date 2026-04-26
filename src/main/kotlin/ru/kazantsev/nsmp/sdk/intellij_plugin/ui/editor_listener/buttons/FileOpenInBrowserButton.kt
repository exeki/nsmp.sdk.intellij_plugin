@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file.FileOpenInBrowserAction

class FileOpenInBrowserButton(
    project: Project,
    file: VirtualFile,
) : AbstractButton(
    file = file,
    project = project,
    action = FileOpenInBrowserAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Open in Browser"
        description = "Open current file in browser"
        isEnabledAndVisible = true
        icon = null
    }
)
