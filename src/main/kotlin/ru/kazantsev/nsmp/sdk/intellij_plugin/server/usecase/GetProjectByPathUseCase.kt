package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.SystemInfo
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.JsonSerializer
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.request.ProjectByPathRequest
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.MessageResponse
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.ProjectByPathResponse
import java.nio.charset.StandardCharsets
import java.nio.file.InvalidPathException
import java.nio.file.Path

class GetProjectByPathUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "POST",
        path = "/projects/by-path",
    )

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        val rawBody = httpExchange.requestBody.use { it.readAllBytes() }.toString(StandardCharsets.UTF_8)
        if (rawBody.isBlank()) {
            return UseCaseResult.of(
                statusCode = 400,
                body = MessageResponse(message = "Request body is required"),
            )
        }

        val request = try {
            JsonSerializer.fromJson<ProjectByPathRequest>(rawBody)
        } catch (e: Exception) {
            return UseCaseResult.of(
                statusCode = 400,
                body = MessageResponse(message = "Invalid JSON body: ${e.message}"),
            )
        }

        val requestedPath = request.path.trim().ifBlank { null }
            ?: return UseCaseResult.of(
                statusCode = 400,
                body = MessageResponse(message = "Missing or blank path in body"),
            )

        val project = ProjectManager.getInstance().openProjects.firstOrNull { p ->
            sameProjectPath(p.basePath, requestedPath)
        } ?: return UseCaseResult.of(
            statusCode = 404,
            body = MessageResponse(message = "No open project matches the given path"),
        )

        return UseCaseResult.of(
            statusCode = 200,
            body = ProjectByPathResponse.from(project),
        )
    }

    private fun sameProjectPath(projectBasePath: String?, requestedPath: String): Boolean {
        if (projectBasePath.isNullOrBlank()) return false
        val nRequested = normalizePath(requestedPath)
        val nBase = normalizePath(projectBasePath)
        return if (SystemInfo.isWindows) nRequested.equals(nBase, ignoreCase = true)
        else nRequested == nBase
    }

    private fun normalizePath(path: String): String {
        return try {
            Path.of(path).normalize().toString().replace('\\', '/').trimEnd('/')
        } catch (_: InvalidPathException) {
            path.trim().replace('\\', '/').trimEnd('/')
        }
    }
}
