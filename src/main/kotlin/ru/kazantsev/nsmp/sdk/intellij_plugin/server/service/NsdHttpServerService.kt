package ru.kazantsev.nsmp.sdk.intellij_plugin.server.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.json.JsonElement
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.JsonSerializer
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.AppSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.topics.ServerStateTopic
import ru.kazantsev.nsmp.sdk.intellij_plugin.topics.SettingsTopic
import com.intellij.openapi.project.ProjectManager
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

@Service(Service.Level.APP)
class NsdHttpServerService : Disposable {
    @Volatile
    private var server: HttpServer? = null

    @Volatile
    private var lastStartError: Exception? = null

    @Volatile
    private var executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "nsd-plugin-http-server").apply { isDaemon = true }
    }

    private val requestHandlerService: NsdHttpRequestHandlerService
        get() = ApplicationManager.getApplication().getService(NsdHttpRequestHandlerService::class.java)

    private val messageBus = ApplicationManager.getApplication().messageBus.connect()

    init {
        messageBus.subscribe(SettingsTopic.TOPIC, object : SettingsTopic {
            override fun settingsChanged() {
                restart()
            }
        })
    }

    override fun dispose() {
        logger.info("NSD Plugin HTTP service disposing")
        stop()
        executor.shutdownNow()
    }

    @Synchronized
    fun restart() {
        stop()
        start()
    }

    fun isRunning(): Boolean = server != null

    fun lastStartError(): Exception? = lastStartError

    @Synchronized
    fun start() {
        val port = AppSettingsService.getInstance().serverPort
        if (port == 0) {
            publishServerState { serverStopped() }
            return
        }
        try {
            val httpServer = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0).apply {
                createContext("/") { exchange -> handleRequest(exchange) }
                executor = this@NsdHttpServerService.executor
                start()
            }

            server = httpServer
            lastStartError = null
            logger.info("NSD Plugin HTTP server started on port $port")
            publishServerState { serverStarted(port) }
        } catch (e: Exception) {
            lastStartError = e
            if (e is BindException)
                logger.warn("NSD Plugin HTTP server failed to start on port $port: ${e.message}")
            else
                logger.warn("NSD Plugin HTTP server failed to start", e)
            publishServerState { serverStartError(e) }
        }
    }

    @Synchronized
    fun stop() {
        if (server != null) {
            logger.info("NSD Plugin HTTP server stopping")
            server!!.stop(0)
            server = null
            publishServerState { serverStopped() }
        }
    }

    private fun publishServerState(action: ServerStateTopic.() -> Unit) {
        ProjectManager.getInstance().openProjects.forEach { project ->
            project.messageBus.syncPublisher(ServerStateTopic.TOPIC).action()
        }
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
