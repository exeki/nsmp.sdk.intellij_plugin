@file:Suppress("SimplifiableCallChain")

package ru.kazantsev.nsmp.sdk.intellij_plugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService

@Service(Service.Level.PROJECT)
class ScriptExecutionService(private val project: Project) {

    val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    val fileSupportService: FileSupportService
        get() = project.service<FileSupportService>()

    fun concatenateFiles(
        file: VirtualFile,
        contextFileRelativePath: String
    ): String {
        val contextVirtualFile = fileSupportService.getVirtualFileByRelativePath(contextFileRelativePath)
        return listOf(contextVirtualFile, file)
            .onEach { fileSupportService.saveDocument(fileSupportService.getDocument(it)) }
            .map { file -> readTextWithoutPackage(file) }
            .joinToString(separator = "\n\n")
    }

    private fun readTextWithoutPackage(file: VirtualFile): String {
        val psiFile = PsiManager.getInstance(project).findFile(file) as GroovyFile
        val packageDefinition = psiFile.packageDefinition ?: return psiFile.text

        return psiFile.text
            .removeRange(packageDefinition.textRange.startOffset, packageDefinition.textRange.endOffset)
            .trimStart()
    }

    private fun getSingleFileText(file: VirtualFile): String {
        val document = fileSupportService.getDocument(file)
        fileSupportService.saveDocument(document)
        return document.text
    }

    fun executeFile(file: VirtualFile, contextFileRelativePath: String? = null): String {
        val connectionParams = projectSettingsService.connectorParams
        val connector = Connector(connectionParams)
        return  if (contextFileRelativePath.isNullOrEmpty())  connector.execFile(getSingleFileText(file))
        else  connector.execFile(concatenateFiles(file, contextFileRelativePath))
    }

}
