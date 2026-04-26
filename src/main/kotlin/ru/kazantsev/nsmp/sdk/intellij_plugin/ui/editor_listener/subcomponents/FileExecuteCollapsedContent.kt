package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import javax.swing.JLabel

class FileExecuteCollapsedContent(
    private val project: Project,
    private val file: VirtualFile,
) : JLabel() {

    private val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    init {
        isOpaque = false
        alignmentY = CENTER_ALIGNMENT
        border = JBUI.Borders.emptyLeft(COLLAPSED_CONTEXT_GAP)
        addPropertyChangeListener("visible") { event ->
            val becameVisible = event.newValue as? Boolean ?: return@addPropertyChangeListener
            if (becameVisible) updateCollapsedContextLabel()
            revalidate()
            repaint()
        }
    }

    companion object {
        private const val COLLAPSED_CONTEXT_GAP = 8
    }

    private fun updateCollapsedContextLabel() {
        val context = projectSettingsService.getExecutionContext(file.nameWithoutExtension)
        text = context?.takeIf { it.isNotBlank() }
            ?.let { MessageBundle.message("sync.command.execute.context.title", it) }
            .orEmpty()
    }
}