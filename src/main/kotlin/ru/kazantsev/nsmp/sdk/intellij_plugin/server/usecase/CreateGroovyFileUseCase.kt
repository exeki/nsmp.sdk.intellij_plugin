package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.intellij.openapi.application.ApplicationManager
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.MessageResponse
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.CreateGroovyFileResponse
import ru.kazantsev.nsmp.sdk.intellij_plugin.project.ProjectService
import java.nio.charset.StandardCharsets

class CreateGroovyFileUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "POST",
        path = "/groovy-files",
    )

    private val projectService: ProjectService
        get() = ApplicationManager.getApplication().getService(ProjectService::class.java)

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        val projectName = httpExchange.getQueryParameter("projectName")
            ?: return UseCaseResult.of(
                statusCode = 400,
                body = MessageResponse(message = "Missing query parameter 'projectName'"),
            )

        val project = projectService.findOpenProjectByName(projectName)
            ?: return UseCaseResult.of(
                statusCode = 404,
                body = MessageResponse(message = "Project '$projectName' not found"),
            )

        val fileText = httpExchange.requestBody.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        if (fileText.isBlank()) {
            return UseCaseResult.of(
                statusCode = 400,
                body = MessageResponse(message = "Request body must contain Groovy file text"),
            )
        }

        val createdFile = runCatching {
            projectService.createGroovyFile(project, fileText)
        }.getOrElse { error ->
            return UseCaseResult.of(
                statusCode = 500,
                body = MessageResponse(message = error.message ?: "Failed to create Groovy file"),
            )
        }

        return UseCaseResult.of(
            statusCode = 201,
            body = CreateGroovyFileResponse(
                status = "ok",
                projectName = createdFile.projectName,
                fileName = createdFile.fileName,
                filePath = createdFile.filePath,
            ),
        )
    }

    private fun HttpExchange.getQueryParameter(name: String): String? {
        val rawQuery = requestURI.rawQuery ?: return null
        return rawQuery
            .split("&")
            .mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                val key = parts.getOrNull(0) ?: return@mapNotNull null
                val value = parts.getOrNull(1).orEmpty()
                key to java.net.URLDecoder.decode(value, StandardCharsets.UTF_8)
            }
            .firstOrNull { (key, _) -> key == name }
            ?.second
            ?.takeIf { it.isNotBlank() }
    }
}
