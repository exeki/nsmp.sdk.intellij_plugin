package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle

class FileSyncCheckButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.sync.check.short.title"),
    file = file,
    project = project
) {
    init {
        addActionListener {
            syncUIAdapter.syncCheck(
                file = file,
                onSuccessCallback = { },
                onFailureCallback = { }
            )
        }
    }
}
