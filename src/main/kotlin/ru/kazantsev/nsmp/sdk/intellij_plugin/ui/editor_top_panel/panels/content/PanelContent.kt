package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels.content

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.buttons.*
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.FileExecuteContextField
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.FileExecuteContextHelp
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.FileExecuteContextTitle

class PanelContent(
    project: Project,
     file: VirtualFile,
) : AbstractPanelContent(project) {

    init {
        addStrut()
        val fileExecuteButton = FileExecuteButton(project, file)
        if (fileExecuteButton.computableWithFile()) {
            add(FileExecuteContextTitle())
            add(FileExecuteContextField(project, file))
            addStrut()
            add(FileExecuteContextHelp())
            addStrut()
            add(fileExecuteButton)
        }
        listOf(
            FileOpenInBrowserButton(project, file),
            FilePullButton(project, file),
            FileSyncCheckButton(project, file),
            FilePushButton(project, file)
        ).forEach {
            if (it.computableWithFile()) {
                addDelimiter()
                add(it)
            }
        }
    }
}