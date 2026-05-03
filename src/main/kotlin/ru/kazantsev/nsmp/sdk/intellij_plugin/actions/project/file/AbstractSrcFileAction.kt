package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.file

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService

abstract class AbstractSrcFileAction : AbstractFileAction() {

    override fun computableWithFile(project: Project, file: VirtualFile): Boolean {
        val sncService = project.service<SyncService>()
        return sncService.getSrcType(file) != null
    }

}
