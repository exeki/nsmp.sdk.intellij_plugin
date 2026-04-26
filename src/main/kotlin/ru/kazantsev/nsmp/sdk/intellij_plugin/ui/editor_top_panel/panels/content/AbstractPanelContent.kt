package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels.content

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.TopPanelScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

abstract class AbstractPanelContent(
    protected val project: Project
) : JPanel(BorderLayout()) {

    companion object {
        private const val STRUT = 3
    }

    protected val projectSettingsService : ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = false
        alignmentY = CENTER_ALIGNMENT
        preferredSize.height = JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT)
        minimumSize = Dimension(0, JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT))
        maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT))
      }

    override fun add(comp: Component): Component? {
        if (comp is JComponent) {
            comp.alignmentY = CENTER_ALIGNMENT
        }
        return super.add(comp)
    }

    fun addStrut(){
        add(Box.createHorizontalStrut(JBUI.scale(STRUT)))
    }

    fun addDelimiter() {
        val separator = JSeparator(SwingConstants.VERTICAL).apply {
            isOpaque = true
            preferredSize = JBUI.size(2, 16)
            maximumSize = JBUI.size(2, 16)
            foreground = JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground()
            background = foreground
            alignmentY = CENTER_ALIGNMENT
        }
        addStrut()
        add(separator)
        addStrut()
    }

}