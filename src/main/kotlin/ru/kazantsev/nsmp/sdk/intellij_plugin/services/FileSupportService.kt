@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.io.files.FileNotFoundException
import java.nio.file.Path
import java.nio.file.Paths

@Service(Service.Level.PROJECT)
class FileSupportService(private val project: Project) {

    companion object {
        const val GROOVY_EXTENSION = "groovy"
        const val SRC_GROOVY_DIRECTORY = "src/main/groovy"
    }

    val fileDocumentManager = FileDocumentManager.getInstance()

    val projectPath: String
        get() = project.basePath ?: throw IllegalStateException("Project is not set")

    fun absolutePathToRelative(path: String): String {
        return Paths.get(projectPath)
            .relativize(Paths.get(path))
            .toString()
            .replace('\\', '/')
    }

    fun relativePathToAbsolute(pathString: String): String {
        val path = Paths.get(pathString)
        if (path.isAbsolute) return pathString
        return Path.of(projectPath, pathString).normalize().toString()
    }

    fun getVirtualFileByRelativePath(relativePath: String): VirtualFile {
        val filePath = relativePathToAbsolute(relativePath)
        return getVirtualFileByAbsolutePath(filePath)
    }

    fun getVirtualFileByAbsolutePath(relativePath: String): VirtualFile {
        val filePath = relativePathToAbsolute(relativePath)
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)
            ?: throw FileNotFoundException("File not found: $relativePath")
    }

    fun getGroovySrcFolderAbsolutePath(): String {
        return relativePathToAbsolute(SRC_GROOVY_DIRECTORY)
    }

    fun getGroovySrcFolderVirtualFile(): VirtualFile {
        return getVirtualFileByRelativePath(SRC_GROOVY_DIRECTORY)
    }

    fun getProjectVirtualFile(): VirtualFile {
        return getVirtualFileByAbsolutePath(projectPath)
    }

    fun getDocument(file: VirtualFile) : Document {
        val document = fileDocumentManager.getDocument(file)
        if (document == null) throw RuntimeException("Can't get document for file ${file.path}")
        return document
    }

    fun saveDocument(document: Document) {
        fileDocumentManager.saveDocument(document)
    }

}