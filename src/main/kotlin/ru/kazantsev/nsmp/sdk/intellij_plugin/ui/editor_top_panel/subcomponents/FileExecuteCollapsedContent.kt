package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.event.HierarchyEvent
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
        addHierarchyListener { e ->
            if (e.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                if (isShowing) {
                    updateCollapsedContextLabel()
                    revalidate()
                    repaint()
                }
            }
        }
    }

    private fun updateCollapsedContextLabel() {
        val context = projectSettingsService.getExecutionContext(file.nameWithoutExtension)
        text = context?.takeIf { it.isNotBlank() }
            ?.let { MessageBundle.message("sync.command.execute.context.title", it) }
            .orEmpty()
    }
}