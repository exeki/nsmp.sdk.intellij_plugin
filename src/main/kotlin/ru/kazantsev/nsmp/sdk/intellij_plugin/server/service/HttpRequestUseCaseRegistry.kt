package ru.kazantsev.nsmp.sdk.intellij_plugin.server.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.HttpRoute
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase.GetOpenProjectsUseCase
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase.GetProjectByPathUseCase
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase.GetRootStatusUseCase
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase.AbstractHttpRequestUseCase
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.usecase.PullUseCase

@Service(Service.Level.APP)
class HttpRequestUseCaseRegistry {

    private val useCasesByRoute: Map<HttpRoute, AbstractHttpRequestUseCase> = listOf(
        GetRootStatusUseCase(),
        GetOpenProjectsUseCase(),
        GetProjectByPathUseCase(),
        PullUseCase()
    ).associateBy { it.route }

    fun find(route: HttpRoute): AbstractHttpRequestUseCase? {
        logger.info("NSD Plugin searching use case for method=${route.method}, path=${route.path}")
        val useCase = useCasesByRoute[route]

        if (useCase == null) logger.info("NSD Plugin use case not found for method=${route.method}, path=${route.path}")
        else logger.info("NSD Plugin use case found: ${useCase::class.simpleName} for method=${route.method}, path=${route.path}")

        return useCase
    }

    private companion object {
        private val logger = thisLogger()
    }
}
