package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global.AbstractGlobalAction
import java.awt.Dimension
import javax.swing.JButton

abstract class AbstractButton(
    protected val action: AbstractGlobalAction,
    presentation: Presentation
) : JButton(presentation.text) {

    companion object {
        private const val ACTION_PLACE = "NSMP_SDK.MainTollWindow"
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
                ACTION_PLACE,
                true
            )
        }
    }
}
