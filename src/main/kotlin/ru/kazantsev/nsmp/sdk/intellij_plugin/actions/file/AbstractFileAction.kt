package ru.kazantsev.nsmp.sdk.intellij_plugin.actions.file

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

abstract class AbstractFileAction : AnAction() {

    abstract fun computableWithFile(project : Project, file : VirtualFile): Boolean

}