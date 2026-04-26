package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Scrollable
import javax.swing.SwingConstants

class TopPanelContent(
    private val file: VirtualFile,
    private val project: Project
) : JPanel(BorderLayout()), Scrollable {

    private companion object {
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
        private const val PANEL_VERTICAL_GAP = 4
        private const val PANEL_SIDE_GAP = 8
        private const val COLLAPSED_CONTEXT_GAP = 8
        private const val ACTION_PLACE = "NSMP.EditorTopPanel"
        private const val TOOLBAR_HEIGHT = 20
    }

    val buttons = listOf(
        FileExecuteButton(file, project),
        FileOpenInBrowserButton(file, project),
        FilePullButton(file, project),
        FileSyncCheckButton(file, project),
        FilePushButton(file, project)
    )

    val actionGroup = DefaultActionGroup().apply {
        buttons.filter { it.compatibleWithFile() }
            .forEachIndexed { index, button ->
                if (index > 0) addSeparator()
                add(button)
            }
    }

    val toolbar = ActionManager.getInstance()
        .createActionToolbar(ACTION_PLACE, actionGroup, true)
        .apply {
            targetComponent = this@TopPanelContent
            setMiniMode(true)
            minimumButtonSize = JBUI.size(30, TOOLBAR_HEIGHT)
        }.component.apply {
            isOpaque = false
        }


    private val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    private var collapsed = projectSettingsService.isTopPanelCollapsed()

    private val logo = TopPanelLogo(::toggleCollapsed)
    private val collapsedContextLabel = JLabel().apply {
        isOpaque = false
        verticalAlignment = SwingConstants.CENTER
        border = JBUI.Borders.emptyLeft(COLLAPSED_CONTEXT_GAP)
    }

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = JBUI.Borders.empty(PANEL_VERTICAL_GAP, PANEL_SIDE_GAP)
        isOpaque = false
        alignmentY = CENTER_ALIGNMENT
        add(logo)
        add(collapsedContextLabel)
        add(toolbar)
        applyCollapsedState()
    }

    override fun add(comp: Component): Component? {
        if (comp is JComponent) comp.alignmentY = CENTER_ALIGNMENT
        return super.add(comp)
    }

    private fun toggleCollapsed() {
        collapsed = !collapsed
        projectSettingsService.setTopPanelCollapsed(collapsed)
        applyCollapsedState()
    }

    private fun applyCollapsedState() {
        toolbar.isVisible = !collapsed
        updateCollapsedContextLabel()
        revalidate()
        repaint()
    }

    private fun updateCollapsedContextLabel() {
        val context = projectSettingsService.getExecutionContext(file.nameWithoutExtension)
        collapsedContextLabel.isVisible = collapsed && !context.isNullOrBlank()
        collapsedContextLabel.text = context
            ?.takeIf { it.isNotBlank() }
            ?.let { MessageBundle.message("sync.command.execute.context.title", it) }
            .orEmpty()
    }

    override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
        return JBUI.scale(HORIZONTAL_SCROLL_INCREMENT)
    }

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
        return visibleRect.width.coerceAtLeast(JBUI.scale(HORIZONTAL_SCROLL_INCREMENT))
    }

    override fun getScrollableTracksViewportWidth(): Boolean = false

    override fun getScrollableTracksViewportHeight(): Boolean = false
}
