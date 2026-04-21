package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.AbstractButton
import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

abstract class AbstractFileActionsPanel(
    protected val file: VirtualFile,
    protected val project: Project
) : JPanel(GridBagLayout()) {

    init {
        isOpaque = false
    }

    protected abstract fun createActions(): List<AbstractButton>

    protected fun installActions() {
        val actions = createActions().filter { it.compatibleWithFile() }
        if (actions.isEmpty()) return

        add(createToolbar(actions))
    }

    private fun createToolbar(actions: List<AbstractButton>): JComponent {
        val actionGroup = DefaultActionGroup().apply {
            actions.forEach(::add)
        }

        return ActionManager.getInstance()
            .createActionToolbar(ACTION_PLACE, actionGroup, true)
            .apply {
                targetComponent = this@AbstractFileActionsPanel
                setMiniMode(true)
                minimumButtonSize = Dimension(48, TOOLBAR_HEIGHT)
            }
            .component
            .apply {
                isOpaque = false
            }
    }

    private companion object {
        private const val ACTION_PLACE = "NSMP.EditorTopPanel"
        private const val TOOLBAR_HEIGHT = 20
    }
}
