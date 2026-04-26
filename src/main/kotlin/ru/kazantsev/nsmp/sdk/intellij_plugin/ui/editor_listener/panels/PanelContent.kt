package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FileExecuteButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FileOpenInBrowserButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FilePullButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FilePushButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.FileSyncCheckButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents.FileExecuteContextField
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents.FileExecuteContextHelp
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents.FileExecuteContextTitle
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.Box
import javax.swing.JSeparator
import javax.swing.SwingConstants

class PanelContent(
    project: Project,
    private val file: VirtualFile,
) : AbstractPanelContent(project) {

    companion object {
        private const val STRUT = 3
    }

    fun addStrut() {
        val separator = JSeparator(SwingConstants.VERTICAL).apply {
            isOpaque = true
            preferredSize = JBUI.size(2, 16)
            minimumSize = preferredSize
            maximumSize = preferredSize
            foreground = JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground()
            background = foreground
            alignmentY = CENTER_ALIGNMENT
        }
        add(Box.createHorizontalStrut(JBUI.scale(STRUT)))
        add(separator)
        add(Box.createHorizontalStrut(JBUI.scale(STRUT)))
    }

    init {
        val fileExecuteButton = FileExecuteButton(project, file)
        if (fileExecuteButton.computableWithFile()) {
            add(FileExecuteContextTitle())
            add(FileExecuteContextField(project, file))
            add(FileExecuteContextHelp())
            add(fileExecuteButton)
        }
        listOf(
            FileOpenInBrowserButton(project, file),
            FilePullButton(project, file),
            FileSyncCheckButton(project, file),
            FilePullButton(project, file)
        ).forEach {
            if(it.computableWithFile()) {
                addStrut()
                add(it)
            }
        }
    }
}