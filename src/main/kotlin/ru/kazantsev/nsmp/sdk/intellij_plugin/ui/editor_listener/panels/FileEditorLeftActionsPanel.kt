package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.AbstractButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FileExecuteButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FileOpenInBrowserButton

class FileEditorLeftActionsPanel(
    file: VirtualFile,
    project: Project
) : AbstractFileActionsPanel(file, project) {

    init {
        installActions()
    }

    override fun createActions(): List<AbstractButton> {
        return listOf(
            FileExecuteButton(file, project),
            FileOpenInBrowserButton(file, project)
        )
    }
}
