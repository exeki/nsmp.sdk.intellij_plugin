package ru.kazantsev.nsmp.sdk.intellij_plugin.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import ru.kazantsev.nsmp.basic_api_connector.ConfigService
import ru.kazantsev.nsmp.basic_api_connector.dto.InstallationDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.service.NsdHttpServerService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.project.ProjectSourceRootMarkerService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.AppSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.BoxLayout
import javax.swing.table.DefaultTableModel

class PluginConfigurable(private val project: Project) : Configurable {
    private var panel: JPanel? = null
    private var portField: JBTextField? = null
    private var configPathField: JBTextField? = null
    private var installationsTable: JBTable? = null
    private var installationsTableModel: DefaultTableModel? = null
    private var installationBox: ComboBox<InstallationOption>? = null
    private var scriptsDirectoryField: JBTextField? = null
    private var modulesDirectoryField: JBTextField? = null
    private var advImportsDirectoryField: JBTextField? = null
    private var installations: MutableList<InstallationDto> = mutableListOf()

    private val projectSettings: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()
    private val appSettings: AppSettingsService
        get() = ApplicationManager.getApplication().service<AppSettingsService>()
    private val configService: ConfigService
        get() = ConfigService(AppSettingsService.getInstance().pathToConfigFile)

    override fun getDisplayName(): String = MessageBundle.message("settings.display.name")

    override fun createComponent(): JComponent {
        if (panel == null) {
            val port = JBTextField()
            val configPath = JBTextField()
            val model = createTableModel()
            val table = JBTable(model)
            val projectInstallationBox = ComboBox<InstallationOption>().apply {
                renderer = SimpleListCellRenderer.create("") { value ->
                    value.label
                }
            }
            val scriptsDirectory = JBTextField()
            val modulesDirectory = JBTextField()
            val advImportsDirectory = JBTextField()

            val controls = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
                add(JButton(MessageBundle.message("settings.global.button.add")).apply { addActionListener { addInstallation() } })
                add(JButton(MessageBundle.message("settings.global.button.edit")).apply { addActionListener { editInstallation() } })
                add(JButton(MessageBundle.message("settings.global.button.delete")).apply { addActionListener { deleteInstallation() } })
                add(JButton(MessageBundle.message("settings.global.button.reload")).apply { addActionListener { reloadInstallationsFromPath() } })
            }

            val tablePanel = JPanel(BorderLayout()).apply {
                add(JBScrollPane(table), BorderLayout.CENTER)
                add(controls, BorderLayout.SOUTH)
            }

            val globalSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessageBundle.message("settings.global.server.port"), port)
                .addLabeledComponent(MessageBundle.message("settings.global.connector.config.path"), configPath)
                .addComponentFillVertically(tablePanel, 0)
                .panel
                .apply { border = BorderFactory.createTitledBorder(MessageBundle.message("settings.global.title")) }

            val projectSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessageBundle.message("settings.project.installation"), projectInstallationBox)
                .addLabeledComponent(MessageBundle.message("settings.project.scripts.path"), scriptsDirectory)
                .addLabeledComponent(MessageBundle.message("settings.project.modules.path"), modulesDirectory)
                .addLabeledComponent(MessageBundle.message("settings.project.advimports.path"), advImportsDirectory)
                .addComponentFillVertically(JPanel(), 0)
                .panel
                .apply { border = BorderFactory.createTitledBorder(MessageBundle.message("settings.project.title")) }

            panel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = BorderFactory.createEmptyBorder(8, 12, 8, 12)
                add(globalSettingsPanel)
                add(JLabel(" ").apply { horizontalAlignment = SwingConstants.LEFT })
                add(projectSettingsPanel)
            }

            portField = port
            configPathField = configPath
            installationsTable = table
            installationsTableModel = model
            installationBox = projectInstallationBox
            scriptsDirectoryField = scriptsDirectory
            modulesDirectoryField = modulesDirectory
            advImportsDirectoryField = advImportsDirectory
        }

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val projectSettings = this.projectSettings
        val appSettings = this.appSettings

        if (portField?.text?.trim() != appSettings.serverPort.toString()) return true
        if (configPathField?.text?.trim() != appSettings.pathToConfigFile) return true

        if (installations != configService.installations) return true
        val selectedId = (installationBox?.selectedItem as? InstallationOption)?.id.orEmpty()
        if (selectedId != projectSettings.state.selectedInstallationId) return true
        if (scriptsDirectoryField?.text?.trim() != projectSettings.state.scriptsDirectoryPath) return true
        if (modulesDirectoryField?.text?.trim() != projectSettings.state.modulesDirectoryPath) return true
        return advImportsDirectoryField?.text?.trim() != projectSettings.state.advImportsDirectoryPath
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val projectSettings = this.projectSettings
        val appSettings = this.appSettings

        val port = parsePort(portField?.text)
        val configPath = configPathField?.text?.trim().orEmpty()
        if (configPath.isBlank()) throw ConfigurationException(MessageBundle.message("settings.error.config.path.required"))

        runCatching { configService.saveInstallations(installations) }
            .getOrElse {
                throw ConfigurationException(
                    MessageBundle.message("settings.error.installations.save.failed", it.message ?: "")
                )
            }

        appSettings.serverPort = port
        appSettings.pathToConfigFile = configPath
        projectSettings.state.selectedInstallationId = (installationBox?.selectedItem as? InstallationOption)?.id.orEmpty()
        projectSettings.state.scriptsDirectoryPath = scriptsDirectoryField?.text?.trim().orEmpty()
        projectSettings.state.modulesDirectoryPath = modulesDirectoryField?.text?.trim().orEmpty()
        projectSettings.state.advImportsDirectoryPath = advImportsDirectoryField?.text?.trim().orEmpty()
        project.getService(ProjectSourceRootMarkerService::class.java).markConfiguredRoots()
        NsdHttpServerServiceHolder.restartServer(project)
    }

    override fun reset() {
        val settings = AppSettingsService.getInstance()
        portField?.text = settings.serverPort.toString()
        configPathField?.text = settings.pathToConfigFile
        installations = configService.getInstallations().toMutableList()
        refreshTable()
        refreshProjectInstallations(projectSettings.state.selectedInstallationId)
        scriptsDirectoryField?.text = projectSettings.state.scriptsDirectoryPath
        modulesDirectoryField?.text = projectSettings.state.modulesDirectoryPath
        advImportsDirectoryField?.text = projectSettings.state.advImportsDirectoryPath
    }

    override fun disposeUIResources() {
        panel = null
        portField = null
        configPathField = null
        installationsTable = null
        installationsTableModel = null
        installationBox = null
        scriptsDirectoryField = null
        modulesDirectoryField = null
        advImportsDirectoryField = null
        installations = mutableListOf()
    }

    private fun addInstallation() {
        val selectedProjectInstallationId = (installationBox?.selectedItem as? InstallationOption)?.id
        val dialog = EditInstallationDialog(
            project = project,
            titleText = MessageBundle.message("installation.dialog.title.add"),
            initialValue = null
        )
        if (!dialog.showAndGet()) return
        val installation = dialog.getInstallation()
        val existingIndex = installations.indexOfFirst { it.id == installation.id }
        if (existingIndex >= 0) installations[existingIndex] = installation else installations.add(installation)
        refreshTable()
        refreshProjectInstallations(selectedProjectInstallationId)
    }

    private fun editInstallation() {
        val selected = selectedInstallationIndex() ?: return
        val dialog = EditInstallationDialog(
            project = project,
            titleText = MessageBundle.message("installation.dialog.title.edit"),
            initialValue = installations[selected]
        )
        if (!dialog.showAndGet()) return
        val updated = dialog.getInstallation()
        installations[selected] = updated
        refreshTable()
        refreshProjectInstallations(updated.id)
        installationsTable?.setRowSelectionInterval(selected, selected)
    }

    private fun deleteInstallation() {
        val selected = selectedInstallationIndex() ?: return
        val selectedProjectInstallationId = (installationBox?.selectedItem as? InstallationOption)?.id
        val removedId = installations[selected].id
        installations.removeAt(selected)
        refreshTable()
        refreshProjectInstallations(
            preferredId = selectedProjectInstallationId.takeIf { it != removedId }
        )
    }

    private fun reloadInstallationsFromPath() {
        val path = configPathField?.text?.trim().orEmpty()
        if (path.isBlank()) {
            project.getService(DialogNotificationService::class.java).showError(
                MessageBundle.message("settings.display.name"),
                MessageBundle.message("settings.error.config.path.required")
            )
            return
        }
        installations = configService.getInstallations().toMutableList()
        refreshTable()
        refreshProjectInstallations((installationBox?.selectedItem as? InstallationOption)?.id)
    }

    private fun selectedInstallationIndex(): Int? {
        val selectedRow = installationsTable?.selectedRow ?: -1
        if (selectedRow < 0 || selectedRow >= installations.size) {
            project.getService(DialogNotificationService::class.java).showInfo(
                MessageBundle.message("settings.display.name"),
                MessageBundle.message("settings.info.select.installation")
            )
            return null
        }
        return selectedRow
    }

    private fun refreshProjectInstallations(preferredId: String?) {
        val box = installationBox ?: return
        box.removeAllItems()
        box.addItem(InstallationOption.None)
        installations.forEach { box.addItem(InstallationOption(id = it.id, label = it.id)) }

        val idToSelect = preferredId?.takeIf { it.isNotBlank() }
        if (idToSelect != null) {
            val selectedOption = (0 until box.itemCount)
                .mapNotNull { box.getItemAt(it) }
                .firstOrNull { it.id == idToSelect }
            box.selectedItem = selectedOption ?: InstallationOption.None
        } else {
            box.selectedItem = InstallationOption.None
        }
    }

    private fun refreshTable() {
        val model = installationsTableModel ?: return
        model.rowCount = 0
        installations.forEach { installation ->
            model.addRow(
                arrayOf<Any>(
                    installation.id,
                    installation.scheme,
                    installation.host,
                    maskSecret(installation.accessKey),
                    installation.ignoreSSL,
                )
            )
        }
    }

    private fun createTableModel(): DefaultTableModel {
        return object : DefaultTableModel(
            arrayOf(
                MessageBundle.message("settings.table.id"),
                MessageBundle.message("settings.table.scheme"),
                MessageBundle.message("settings.table.host"),
                MessageBundle.message("settings.table.access.key"),
                MessageBundle.message("settings.table.ignore.ssl"),
            ),
            0
        ) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false
        }
    }

    private fun parsePort(value: String?): Int {
        val port = value?.trim()?.toIntOrNull()
            ?: throw ConfigurationException(MessageBundle.message("settings.error.server.port.integer"))
        if (port !in 1..65535) throw ConfigurationException(MessageBundle.message("settings.error.server.port.range"))
        return port
    }

    private fun maskSecret(secret: String): String = if (secret.isBlank()) "" else "********"
}

private data class InstallationOption(
    val id: String,
    val label: String,
) {
    companion object {
        val None: InstallationOption = InstallationOption(
            id = "",
            label = MessageBundle.message("settings.project.installation.not.selected")
        )
    }
}

private object NsdHttpServerServiceHolder {
    fun restartServer(project: Project) {
        val service = ApplicationManager.getApplication().getService(NsdHttpServerService::class.java)
        runCatching { service.restart() }
            .onFailure {
                project.getService(DialogNotificationService::class.java).showError(
                    MessageBundle.message("settings.display.name"),
                    MessageBundle.message("settings.error.server.restart.failed", it.message ?: "")
                )
            }
    }
}
