package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.intellij.openapi.application.ApplicationManager
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response.CurrentProjectsResponse
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.project.ProjectService

class GetCurrentProjectsUseCase : HttpRequestUseCase {
    override val route: HttpRoute = HttpRoute(
        method = "GET",
        path = "/current-projects",
    )

    private val projectService: ProjectService
        get() = ApplicationManager.getApplication().getService(ProjectService::class.java)

    override fun execute(httpExchange: HttpExchange): UseCaseResult {
        return UseCaseResult.of(
            statusCode = 200,
            body = CurrentProjectsResponse(projects = projectService.getOpenProjects()),
        )
    }
}
