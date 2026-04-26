@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents

import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.io.files.FileNotFoundException
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class FileExecuteContextField(
    private val project: Project,
    private val file: VirtualFile
) : TextFieldWithBrowseButton() {

    private companion object {
        private const val CONTEXT_FIELD_WIDTH = 200
        private const val CONTEXT_FIELD_HEIGHT = 35
    }

    private val projectSettingsService : ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    private val fileSupportService : FileSupportService
        get() = project.service<FileSupportService>()

    private fun saveContext() {
        projectSettingsService.setExecutionContext(file.nameWithoutExtension, this.text)
    }

    private fun srcRoot(): VirtualFile {
        return try {
            fileSupportService.getGroovySrcFolderVirtualFile()
        } catch (_: FileNotFoundException) {
            fileSupportService.getProjectVirtualFile()
        }
    }

    init {
        isOpaque = false
        background = UIUtil.TRANSPARENT_COLOR
        border = JBUI.Borders.empty()
        alignmentY = CENTER_ALIGNMENT
        textField.isOpaque = false
        textField.background = UIUtil.TRANSPARENT_COLOR
        preferredSize = JBUI.size(CONTEXT_FIELD_WIDTH, CONTEXT_FIELD_HEIGHT)
        minimumSize = preferredSize
        maximumSize = preferredSize
        text = projectSettingsService.getExecutionContext(file.nameWithoutExtension).orEmpty()
        textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = saveContext()
            override fun removeUpdate(e: DocumentEvent) = saveContext()
            override fun changedUpdate(e: DocumentEvent) = saveContext()
        })
        addActionListener {
            val root = srcRoot()
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle(MessageBundle.message("sync.command.execute.context.file.dialog.title"))
                .withRoots(root)
                .withFileFilter { it.extension.equals(FileSupportService.GROOVY_EXTENSION, ignoreCase = true) }

            val selectedVirtualFile = if (text.isEmpty()) root
            else fileSupportService.getVirtualFileByRelativePath(text)

            val selectedFile = FileChooser.chooseFile(descriptor, project, selectedVirtualFile)
            if (selectedFile == null) return@addActionListener
            text = fileSupportService.absolutePathToRelative(selectedFile.path)
        }
    }
}