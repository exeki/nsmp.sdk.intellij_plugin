package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file.FileOpenInBrowserAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FileOpenInBrowserButton(
    project: Project,
    file: VirtualFile,
) : AbstractButton(
    file = file,
    project = project,
    action = FileOpenInBrowserAction(),
    presentation = Presentation().apply {
        text = MessageBundle.message("editor.top.panel.button.open.in.browser")
        description = MessageBundle.message("editor.top.panel.button.open.in.browser.description")
        isEnabledAndVisible = true
        icon = null
    }
)
