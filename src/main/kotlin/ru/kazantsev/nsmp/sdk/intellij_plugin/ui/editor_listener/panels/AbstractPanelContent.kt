package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFormat
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.Scrollable

abstract class AbstractPanelContent(
    protected val project: Project
) : JPanel(BorderLayout()) {

    protected val projectSettingsService : ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    protected val syncUIAdapter : SyncUIAdapter
        get() = project.service<SyncUIAdapter>()

    private companion object {
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
        private const val PANEL_HEIGHT = 35
        private const val PANEL_VERTICAL_GAP = 4
        private const val PANEL_SIDE_GAP = 8
        private const val ELEMENTS_HEIGHT = 60
        private const val BUTTONS_GAP = 6
    }

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = JBUI.Borders.empty(PANEL_VERTICAL_GAP, PANEL_SIDE_GAP)
        isOpaque = false
        alignmentY = CENTER_ALIGNMENT
        preferredSize.height = JBUI.scale(PANEL_HEIGHT)
      }

    override fun add(comp: Component): Component? {
        if (comp is JComponent) {
            comp.alignmentY = CENTER_ALIGNMENT
            comp.preferredSize.height = JBUI.scale(ELEMENTS_HEIGHT)
            comp.minimumSize.height = JBUI.scale(ELEMENTS_HEIGHT)
        }
        return super.add(comp)
    }
}