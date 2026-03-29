package ru.kazantsev.nsdplugin.server.usecase

import com.intellij.openapi.application.ApplicationManager
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsdplugin.server.HttpRoute
import ru.kazantsev.nsdplugin.server.dto.response.CurrentProjectsResponse
import ru.kazantsev.nsdplugin.server.UseCaseResult
import ru.kazantsev.nsdplugin.project.ProjectService

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
