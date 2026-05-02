package ru.kazantsev.nsmp.sdk.intellij_plugin.service

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.io.files.FileNotFoundException
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.NotTextFileException
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

    fun getVirtualFileByRelativePath(relativePath: String): VirtualFile? {
        val filePath = relativePathToAbsolute(relativePath)
        return getVirtualFileByAbsolutePath(filePath)
    }

    fun getVirtualFileByAbsolutePath(relativePath: String): VirtualFile? {
        val filePath = relativePathToAbsolute(relativePath)
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)
    }

    fun getGroovySrcFolderVirtualFile(): VirtualFile? {
        return getVirtualFileByRelativePath(SRC_GROOVY_DIRECTORY)
    }

    fun getProjectVirtualFile(): VirtualFile {
        return getVirtualFileByAbsolutePath(projectPath) ?: throw FileNotFoundException("Project is not found")
    }

    fun getDocument(file: VirtualFile): Document {
        return fileDocumentManager.getDocument(file) ?: throw NotTextFileException("File ${file.path} not a text file")
    }

    fun saveDocument(document: Document) {
        fileDocumentManager.saveDocument(document)
    }

    fun refreshProjectExplorer() {
        VirtualFileManager.getInstance().asyncRefresh {
            ApplicationManager.getApplication().invokeLater {
                ProjectView.getInstance(project).refresh()
            }
        }
    }

}