package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectPullAction

class PullButton : AbstractButton(
    action = ProjectPullAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Pull"
        description = "Open project pull dialog window"
        isEnabledAndVisible = true
        icon = null
    }
)