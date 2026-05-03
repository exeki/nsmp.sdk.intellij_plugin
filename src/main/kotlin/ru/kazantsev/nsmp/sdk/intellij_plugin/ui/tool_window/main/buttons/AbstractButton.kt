package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.AbstractProjectAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.MainToolWindowFactory
import java.awt.Dimension
import javax.swing.JButton

abstract class AbstractButton(
    protected val action: AbstractProjectAction,
    presentation: Presentation
) : JButton(presentation.text) {

    companion object {
        const val BUTTON_HEIGHT = 40
    }

    init {
        toolTipText = presentation.description
        isVisible = presentation.isVisible
        isEnabled = presentation.isEnabled
        alignmentX = LEFT_ALIGNMENT
        preferredSize = Dimension(preferredSize.width, BUTTON_HEIGHT)
        maximumSize = Dimension(Int.MAX_VALUE, BUTTON_HEIGHT)
        minimumSize = JBUI.size(40, BUTTON_HEIGHT)

        addActionListener {
            ActionManager.getInstance().tryToExecute(
                action,
                null,
                this,
                MainToolWindowFactory.ACTION_PLACE,
                true
            )
        }
    }
}
