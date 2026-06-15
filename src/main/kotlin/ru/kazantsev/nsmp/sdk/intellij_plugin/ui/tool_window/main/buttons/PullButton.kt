package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectPullAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class PullButton : AbstractButton(
    action = ProjectPullAction(),
    presentation = Presentation().apply {
        text = MessageBundle.message("tool_window.button.pull")
        description = MessageBundle.message("tool_window.button.pull.description")
        isEnabledAndVisible = true
        icon = null
    }
)
