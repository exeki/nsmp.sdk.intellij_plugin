@file:Suppress("FoldInitializerAndIfToElvis")

package ru.kazantsev.nsmp.sdk.intellij_plugin.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import ru.kazantsev.nsmp.basic_api_connector.Connector
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.ContextFileNotExists
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.FileExecuteExtensionException
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.AppSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcFormat

@Service(Service.Level.PROJECT)
class ScriptExecutionService(private val project: Project) {

    val appSettingsService = ApplicationManager.getApplication().service<AppSettingsService>()

    val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    val fileSupportService: FileSupportService
        get() = project.service<FileSupportService>()

    private fun concatenateFiles(
        file: VirtualFile,
        contextVirtualFile: VirtualFile
    ): String {
        return listOf(contextVirtualFile, file)
            .onEach { fileSupportService.saveDocument(fileSupportService.getDocument(it)) }
            .joinToString(separator = "\n\n") { file -> readTextWithoutPackage(file) }
    }

    private fun readTextWithoutPackage(file: VirtualFile): String {
        val psiFile = PsiManager.getInstance(project).findFile(file) as? GroovyFile
            ?: throw FileExecuteExtensionException(file)
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

    fun buildExecutionPayload(file: VirtualFile, contextFileRelativePath: String? = null): String {
        if (file.extension != SrcFormat.GROOVY.code) throw FileExecuteExtensionException(file)
        return if (contextFileRelativePath.isNullOrEmpty()) getSingleFileText(file)
        else {
            val contextVirtualFile = fileSupportService.getVirtualFileByRelativePath(contextFileRelativePath)
            if (contextVirtualFile == null) throw ContextFileNotExists(contextFileRelativePath)
            if (contextVirtualFile.extension != SrcFormat.GROOVY.code) throw FileExecuteExtensionException(file)
            concatenateFiles(file, contextVirtualFile)
        }
    }

    fun executePayload(payload: String): String {
        val connectionParams = projectSettingsService.connectorParams
        val connector = Connector(connectionParams)
        val timeout = appSettingsService.scriptExecuteReadTimeout
        return connector.exec(payload, timeout)
    }
}