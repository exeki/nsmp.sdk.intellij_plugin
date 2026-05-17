package ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase

import com.intellij.openapi.components.service
import com.sun.net.httpserver.HttpExchange
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.ProjectUtilities
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseExecutionException
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.UseCaseResult
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.request.PullRequest
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.PullBackgroundTask
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType

class PullUseCase : AbstractHttpRequestUseCase() {
    override val route: HttpRoute = HttpRoute(
        method = "POST",
        path = "/pull",
    )

    override fun executeInternal(httpExchange: HttpExchange): UseCaseResult {
        val body = getBodyElseThrow<PullRequest>(httpExchange)
        val project = ProjectUtilities.findProjectByPathElseThrow(body.projectPath)
        val service = project.service<SyncService>()
        val type = SrcType.entries.find { it.code == body.type }
            ?: throw UseCaseExecutionException(400, "Unknown src type")
        val req = service.getSrcRequestForCode(body.code, type)
        PullBackgroundTask(project, req).start()
        return UseCaseResult.success("Pull task started successfully")
    }
}
