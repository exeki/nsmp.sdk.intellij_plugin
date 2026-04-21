package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.AbstractButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FilePullButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FilePushButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FileSyncCheckButton

class FileEditorRightActionsPanel(
    file: VirtualFile,
    project: Project
) : AbstractFileActionsPanel(file, project) {

    init {
        installActions()
    }

    override fun createActions(): List<AbstractButton> {
        return listOf(
            FilePullButton(file, project),
            FileSyncCheckButton(file, project),
            FilePushButton(file, project)
        )
    }
}
