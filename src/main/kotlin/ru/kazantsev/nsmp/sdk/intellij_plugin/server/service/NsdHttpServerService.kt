package ru.kazantsev.nsmp.sdk.intellij_plugin.server.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.JsonSerializer
import kotlinx.serialization.json.JsonElement
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.AppSettingsService
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

@Service(Service.Level.APP)
class NsdHttpServerService : Disposable {
    @Volatile
    private var server: HttpServer? = null

    @Volatile
    private var executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "nsd-plugin-http-server").apply { isDaemon = true }
    }

    private val requestHandlerService: NsdHttpRequestHandlerService
        get() = ApplicationManager.getApplication().getService(NsdHttpRequestHandlerService::class.java)

    init {
        logger.info("NSD Plugin HTTP service initializing")
        start()
    }

    @Synchronized
    fun restart() {
        stop()
        start()
    }

    override fun dispose() {
        logger.info("NSD Plugin HTTP service disposing")
        stop()
        executor.shutdownNow()
    }

    @Synchronized
    private fun start() {
        val port = AppSettingsService.Companion.getInstance().serverPort

        try {
            val httpServer = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0).apply {
                createContext("/") { exchange -> handleRequest(exchange) }
                executor = this@NsdHttpServerService.executor
                start()
            }

            server = httpServer
            logger.info("NSD Plugin HTTP server started on port $port")
        } catch (e: BindException) {
            logger.warn("NSD Plugin HTTP server failed to start on port $port: ${e.message}")
        } catch (e: Exception) {
            logger.warn("NSD Plugin HTTP server failed to start", e)
        }
    }

    @Synchronized
    private fun stop() {
        if (server != null) {
            logger.info("NSD Plugin HTTP server stopping")
        }
        server?.stop(0)
        server = null
    }

    private fun handleRequest(exchange: HttpExchange) {
        exchange.use {
            logger.info("NSD Plugin received request: method=${exchange.requestMethod}, path=${exchange.requestURI.path}")
            val result = requestHandlerService.handle(exchange)
            respondJson(exchange, result.statusCode, result.body)
        }
    }

    private fun respondJson(exchange: HttpExchange, statusCode: Int, response: JsonElement) {
        val payload = JsonSerializer.toJson(response).toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(statusCode, payload.size.toLong())
        exchange.responseBody.use { output ->
            output.write(payload)
        }
    }

    private companion object {
        private val logger = thisLogger()
    }
}
