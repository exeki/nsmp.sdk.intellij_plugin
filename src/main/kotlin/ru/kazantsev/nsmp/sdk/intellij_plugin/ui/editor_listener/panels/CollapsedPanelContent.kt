package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FileExecuteButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents.FileExecuteCollapsedContent

class CollapsedPanelContent(
    project: Project,
    private val file: VirtualFile,
) : AbstractPanelContent(project) {
    init {
        val fileExecuteButton = FileExecuteButton(project, file)
        if (fileExecuteButton.computableWithFile()) {
            add(FileExecuteCollapsedContent(project, file))
        }
    }
}