package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file.AbstractFileAction
import javax.swing.JComponent

abstract class AbstractButton(
    protected val project: Project,
    protected val file: VirtualFile,
    protected val action: AbstractFileAction,
    presentation: Presentation
) : ActionButtonWithText(
    action,
    presentation,
    ACTION_PLACE,
    minimumSize
) {
    companion object {
        private const val ACTION_PLACE = "NSMP.EditorTopPanel"
        const val MINIMUM_BUTTON_HEIGHT = 20
        const val MINIMUM_BUTTON_WIDTH = 40
        private val minimumSize = JBUI.size(MINIMUM_BUTTON_WIDTH, MINIMUM_BUTTON_HEIGHT)
    }

    init {
        alignmentY = CENTER_ALIGNMENT
    }

    fun computableWithFile(): Boolean {
        return action.computableWithFile(project, file)
    }

    fun addIfComputableWithFile(component: JComponent) {
        if(computableWithFile()) component.add(this)
    }
}
