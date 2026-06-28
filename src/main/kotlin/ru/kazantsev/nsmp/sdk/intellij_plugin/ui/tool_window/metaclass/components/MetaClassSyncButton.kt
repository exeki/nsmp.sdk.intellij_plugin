package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.components

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectMetaClassSyncAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.metaclass.MetaClassToolWindowFactory
import java.awt.Dimension
import javax.swing.JButton

class MetaClassSyncButton : JButton(MessageBundle.message("metaclass.tool_window.button.sync")) {

    private val action = ProjectMetaClassSyncAction()

    init {
        toolTipText = MessageBundle.message("metaclass.tool_window.button.sync.description")
        alignmentX = LEFT_ALIGNMENT
        preferredSize = Dimension(preferredSize.width, BUTTON_HEIGHT)
        maximumSize = Dimension(Int.MAX_VALUE, BUTTON_HEIGHT)
        minimumSize = JBUI.size(40, BUTTON_HEIGHT)
        addActionListener {
            ActionManager.getInstance().tryToExecute(
                action,
                null,
                this,
                MetaClassToolWindowFactory.ACTION_PLACE,
                true,
            )
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    private companion object {
        const val BUTTON_HEIGHT = 40
    }
}
