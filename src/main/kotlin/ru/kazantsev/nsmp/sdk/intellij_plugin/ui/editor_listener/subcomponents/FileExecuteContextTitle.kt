package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents

import com.intellij.ui.components.JBLabel
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.Component

class FileExecuteContextTitle : JBLabel(MessageBundle.message("sync.command.execute.context.label")){
    init {
        alignmentY = CENTER_ALIGNMENT
    }
}