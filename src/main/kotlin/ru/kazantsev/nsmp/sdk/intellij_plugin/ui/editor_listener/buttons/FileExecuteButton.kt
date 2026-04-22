@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.io.files.FileNotFoundException
import ru.kazantsev.nsmp.basic_api_connector.exception.BadResponseException
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.FileSupportService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.ScriptExecutionService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.Component
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class FileExecuteButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.execute.short.title"),
    file = file,
    project = project
) {

    val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    val fileSupportService: FileSupportService
        get() = project.service<FileSupportService>()

    val scriptExecutionService: ScriptExecutionService
        get() = project.service<ScriptExecutionService>()

    override fun compatibleWithFile(): Boolean {
        return file.extension.equals("groovy", ignoreCase = true)
    }

    private val contextField = TextFieldWithBrowseButton().apply {
        isOpaque = false
        background = UIUtil.TRANSPARENT_COLOR
        border = JBUI.Borders.empty()
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

    private val help = object : JLabel(AllIcons.General.ContextHelp) {
        override fun getToolTipLocation(event: MouseEvent?): Point {
            return Point(0, height + 8)
        }
    }.apply {
        toolTipText = htmlTooltip(MessageBundle.message("sync.command.execute.context.help"))
        alignmentY = Component.CENTER_ALIGNMENT
    }

    private val title = JBLabel(MessageBundle.message("sync.command.execute.context.label"))

    private fun srcRoot(): VirtualFile {
        return try {
            fileSupportService.getGroovySrcFolderVirtualFile()
        } catch (_: FileNotFoundException) {
            fileSupportService.getProjectVirtualFile()
        }
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val button = createButtonComponent(presentation, place)
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            alignmentY = Component.CENTER_ALIGNMENT
            button.alignmentY = Component.CENTER_ALIGNMENT
            title.alignmentX = Component.CENTER_ALIGNMENT
            contextField.alignmentY = Component.CENTER_ALIGNMENT
            add(Box.createHorizontalStrut(20))
            add(title)
            add(Box.createHorizontalStrut(5))
            add(contextField)
            add(Box.createHorizontalStrut(5))
            add(help)
            add(Box.createHorizontalStrut(5))
            add(button)
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val dialogNotificationService = project.service<DialogNotificationService>()
        try {
            val result = scriptExecutionService.executeFile(file, contextField.text)
            dialogNotificationService.showInfo(
                title = MessageBundle.message("sync.command.execute.file.result.title"),
                message = result
            )
        } catch (e: Throwable) {
            if (e is BadResponseException) dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                message = e.responseSnapshot.bodyAsString
            )
            else dialogNotificationService.showError(
                title = MessageBundle.message("sync.command.execute.file.error.title"),
                error = e
            )
        }
    }

    private fun htmlTooltip(text: String): String {
        return "<html><body width='420'>$text</body></html>"
    }

    private fun saveContext() {
        projectSettingsService.setExecutionContext(file.nameWithoutExtension, contextField.text)
    }

    private companion object {
        private const val CONTEXT_FIELD_WIDTH = 200
        private const val CONTEXT_FIELD_HEIGHT = 25
    }
}
