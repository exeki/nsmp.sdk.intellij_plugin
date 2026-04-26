package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFormat

abstract class AbstractGroovyFileAction : AbstractFileAction() {

    override fun computableWithFile(project : Project, file : VirtualFile): Boolean {
        return file.exists() && file.extension == SrcFormat.GROOVY.code
    }
}
