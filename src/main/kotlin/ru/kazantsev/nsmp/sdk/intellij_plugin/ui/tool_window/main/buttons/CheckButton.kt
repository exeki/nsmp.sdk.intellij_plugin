package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectSyncCheckAction

class CheckButton : AbstractButton(
    action = ProjectSyncCheckAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "SyncCheck"
        description = "Open project sync check dialog window"
        isEnabledAndVisible = true
        icon = null
    }
)