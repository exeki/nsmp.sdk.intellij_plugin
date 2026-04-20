package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import java.awt.Dimension
import javax.swing.JButton

abstract class AbstractButton(
    title: String,
    protected val file: VirtualFile,
    protected val project: Project
) : JButton(title) {

    val syncUIAdapter: SyncUIAdapter
        get() = project.service<SyncUIAdapter>()

    init {
        isFocusable = false
        margin = JBUI.insets(0, 6)
    }

    override fun getPreferredSize(): Dimension {
        return super.getPreferredSize().apply {
            height = 26
        }
    }
}
