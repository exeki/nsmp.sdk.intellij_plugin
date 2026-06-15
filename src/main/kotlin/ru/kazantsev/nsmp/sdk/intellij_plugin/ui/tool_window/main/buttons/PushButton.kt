package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectPushAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class PushButton : AbstractButton(
    action = ProjectPushAction(),
    presentation = Presentation().apply {
        text = MessageBundle.message("tool_window.button.push")
        description = MessageBundle.message("tool_window.button.push.description")
        isEnabledAndVisible = true
        icon = null
    }
)
