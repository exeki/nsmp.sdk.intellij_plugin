package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global.GlobalPushAction

class PushButton() : AbstractButton(
    action = GlobalPushAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Push"
        description = "Open project push dialog window"
        isEnabledAndVisible = true
        icon = null
    }
)