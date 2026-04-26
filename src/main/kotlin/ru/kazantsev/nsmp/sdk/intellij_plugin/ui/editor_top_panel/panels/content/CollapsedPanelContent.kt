package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels.content

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.buttons.FileExecuteButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.FileExecuteCollapsedContent

class CollapsedPanelContent(
    project: Project,
    file: VirtualFile,
) : AbstractPanelContent(project) {
    init {
        val fileExecuteButton = FileExecuteButton(project, file)
        if (fileExecuteButton.computableWithFile()) {
            addStrut()
            add(FileExecuteCollapsedContent(project, file))
        }
    }
}