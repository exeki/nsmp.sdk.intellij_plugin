package ru.kazantsev.nsdplugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTextField
import ru.kazantsev.nsdplugin.server.service.NsdHttpServerService
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class NsdPluginConfigurable : Configurable {
    private var panel: JPanel? = null
    private var portField: JBTextField? = null
    private var uploadUrlField: JBTextField? = null

    override fun getDisplayName(): String = "NSD Plugin"

    override fun createComponent(): JComponent {
        if (panel == null) {
            val port = JBTextField()
            val uploadUrl = JBTextField()

            panel = JPanel(GridBagLayout()).apply {
                val gbc = GridBagConstraints().apply {
                    anchor = GridBagConstraints.WEST
                    fill = GridBagConstraints.HORIZONTAL
                    weightx = 1.0
                    insets = com.intellij.util.ui.JBUI.insets(4)
                }

                gbc.gridx = 0
                gbc.gridy = 0
                gbc.weightx = 0.0
                add(JLabel("Порт локального сервиса"), gbc)

                gbc.gridx = 1
                gbc.weightx = 1.0
                add(port, gbc)

                gbc.gridx = 0
                gbc.gridy = 1
                gbc.weightx = 0.0
                add(JLabel("URL для выполнения скриптов"), gbc)

                gbc.gridx = 1
                gbc.weightx = 1.0
                add(uploadUrl, gbc)
            }

            portField = port
            uploadUrlField = uploadUrl
        }

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = NsdPluginSettingsService.getInstance()
        val portText = portField?.text?.trim().orEmpty()
        val uploadUrlText = uploadUrlField?.text?.trim().orEmpty()
        return portText != settings.serverPort.toString() || uploadUrlText != settings.uploadUrl
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = NsdPluginSettingsService.getInstance()
        val port = parsePort(portField?.text)
        val uploadUrl = uploadUrlField?.text?.trim().orEmpty()

        settings.serverPort = port
        settings.uploadUrl = uploadUrl
        NsdHttpServerServiceHolder.restartServer()
    }

    override fun reset() {
        val settings = NsdPluginSettingsService.getInstance()
        portField?.text = settings.serverPort.toString()
        uploadUrlField?.text = settings.uploadUrl
    }

    override fun disposeUIResources() {
        panel = null
        portField = null
        uploadUrlField = null
    }

    private fun parsePort(value: String?): Int {
        val port = value?.trim()?.toIntOrNull()
            ?: throw ConfigurationException("Порт должен быть целым числом.")

        if (port !in 1..65535) {
            throw ConfigurationException("Порт должен быть в диапазоне 1..65535.")
        }

        return port
    }
}

private object NsdHttpServerServiceHolder {
    fun restartServer() {
        val service = com.intellij.openapi.application.ApplicationManager
            .getApplication()
            .getService(NsdHttpServerService::class.java)

        runCatching { service.restart() }
            .onFailure { Messages.showErrorDialog("Не удалось перезапустить локальный сервис: ${it.message}", "NSD Plugin") }
    }
}
