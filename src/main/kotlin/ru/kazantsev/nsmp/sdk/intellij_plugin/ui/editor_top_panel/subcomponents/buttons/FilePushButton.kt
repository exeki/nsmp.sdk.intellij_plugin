package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file.FilePushAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class FilePushButton(
    project: Project,
    file: VirtualFile,
) : AbstractButton(
    file = file,
    project = project,
    action = FilePushAction(),
    presentation = Presentation().apply {
        text = MessageBundle.message("editor.top.panel.button.push")
        description = MessageBundle.message("editor.top.panel.button.push.description")
        isEnabledAndVisible = true
        icon = null
    }
)
