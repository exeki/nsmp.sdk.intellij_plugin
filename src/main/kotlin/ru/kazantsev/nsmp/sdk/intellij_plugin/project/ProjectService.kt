package ru.kazantsev.nsmp.sdk.intellij_plugin.project

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.project.ProjectInfoDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.project.SourceRootDto
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service(Service.Level.APP)
class ProjectService {
    fun getOpenProjects(): List<ProjectInfoDto> {
        return ProjectManager.getInstance().openProjects.map { project ->
            ProjectInfoDto(
                name = project.name,
                basePath = project.basePath,
                sourceRoots = getSourceRoots(project),
            )
        }
    }

    fun findOpenProjectByName(projectName: String): Project? {
        return ProjectManager.getInstance().openProjects.firstOrNull { it.name == projectName }
    }

    fun createGroovyFile(project: Project, fileText: String): ProjectFileCreationResult {
        val projectBasePath = requireNotNull(project.basePath) { "Project '${project.name}' does not have a base path" }
        val rootDirectory = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByPath(projectBasePath)) {
            "Project root directory is unavailable"
        }

        val timestamp = LocalDateTime.now().format(FILE_NAME_FORMATTER)
        val fileName = "generated-$timestamp.groovy"

        val createdFile = WriteCommandAction.writeCommandAction(project)
            .compute<com.intellij.openapi.vfs.VirtualFile, Throwable> {
                val file = rootDirectory.findChild(fileName) ?: rootDirectory.createChildData(this, fileName)
                file.setBinaryContent(fileText.toByteArray(StandardCharsets.UTF_8))
                file
            }

        return ProjectFileCreationResult(
            projectName = project.name,
            fileName = createdFile.name,
            filePath = createdFile.path,
        )
    }

    private fun getSourceRoots(project: Project): List<SourceRootDto> {
        return ModuleManager.getInstance(project).modules
            .flatMap { module -> ModuleRootManager.getInstance(module).sourceRoots.toList() }
            .map { SourceRootDto(path = it.path, name = it.name) }
            .distinct()
    }

    private companion object {
        val FILE_NAME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
    }
}
