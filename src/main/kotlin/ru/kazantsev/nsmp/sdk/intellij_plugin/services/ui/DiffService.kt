package ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.LastSyncedStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.RemoteSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.DiffManagerPanel
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.CompareSetType
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffRow
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffStatus
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

@Service(Service.Level.PROJECT)
class DiffService(
    private val project: Project
) {
    companion object {
        const val TOOL_WINDOW_ID = "NSMP SDK Local Changes"
    }

    private val projectSettingsService = project.service<ProjectSettingsService>()
    private val lastSyncedStateService = project.service<LastSyncedStateService>()
    private val remoteSrcStateService = project.service<RemoteSrcStateService>()
    private val dialogNotificationService = project.service<DialogNotificationService>()

    @Volatile
    private var panel: DiffManagerPanel? = null
    @Volatile
    private var leftSet: CompareSetType = CompareSetType.LOCAL
    @Volatile
    private var rightSet: CompareSetType = CompareSetType.LAST_SYNC

    fun bindPanel(value: DiffManagerPanel) {
        panel = value
        panel?.setCompareSets(leftSet, rightSet)
    }

    fun showLocalChanges(left: CompareSetType, right: CompareSetType) {
        leftSet = left
        rightSet = right
        val title = MessageBundle.message("sync.command.local.changes.title")
        FileDocumentManager.getInstance().saveAllDocuments()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, title, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val rows = collectDiffRows(left, right)
                    ApplicationManager.getApplication().invokeLater {
                        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)
                        panel?.setCompareSets(left, right)
                        panel?.setRows(rows)
                        toolWindow?.show()
                    }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater {
                        dialogNotificationService.showError(title = title, error = error)
                    }
                }
            }
        })
    }

    fun updateRemoteStateAndRefreshIfNeeded() {
        val title = MessageBundle.message("sync.command.local.changes.title")
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, title, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    remoteSrcStateService.updateRemoteStateFolder(
                        SrcRequest(allModules = true, allScripts = true, allAdvImports = true)
                    )
                    if (leftSet == CompareSetType.REMOTE || rightSet == CompareSetType.REMOTE) {
                        showLocalChanges(leftSet, rightSet)
                    }
                } catch (error: Throwable) {
                    ApplicationManager.getApplication().invokeLater {
                        dialogNotificationService.showError(title = title, error = error)
                    }
                }
            }
        })
    }

    private fun collectDiffRows(left: CompareSetType, right: CompareSetType): List<DiffRow> {
        val types = listOf(SrcType.SCRIPT, SrcType.MODULE, SrcType.ADV_IMPORT)
        return types
            .flatMap { type ->
                val leftMap = loadSet(type, left)
                val rightMap = loadSet(type, right)
                (leftMap.keys + rightMap.keys)
                    .sorted()
                    .mapNotNull { code ->
                        toDiffRow(type, code, left, right, leftMap[code], rightMap[code])
                    }
            }
            .sortedWith(compareBy<DiffRow> { it.type.code }.thenBy { it.code })
    }

    private fun loadSet(type: SrcType, set: CompareSetType): Map<String, Path> {
        return when (set) {
            CompareSetType.LOCAL -> {
                val request = SrcSetRequest(type = type, all = true)
                projectSettingsService.srcSyncService.localSrcService.getLocalSrcSet(request)
                    .associateBy(LocalFileInfo::code) { it.file.toPath() }
            }
            CompareSetType.LAST_SYNC -> lastSyncedStateService
                .listLastSyncedFiles(type)
                .mapValues { it.value.toNioPath() }
            CompareSetType.REMOTE -> remoteSrcStateService
                .listRemoteFiles(type)
                .mapValues { it.value.toNioPath() }
        }
    }

    private fun toDiffRow(
        type: SrcType,
        code: String,
        leftSet: CompareSetType,
        rightSet: CompareSetType,
        leftPath: Path?,
        rightPath: Path?
    ): DiffRow? {
        val status = when {
            leftPath == null && rightPath != null -> DiffStatus.DELETED
            leftPath != null && rightPath == null -> DiffStatus.ADDED
            leftPath != null && rightPath != null && Files.mismatch(leftPath, rightPath) != -1L -> DiffStatus.MODIFIED
            else -> null
        } ?: return null

        val lastSyncDate = if (rightSet == CompareSetType.LAST_SYNC && rightPath != null && Files.exists(rightPath)) {
            Instant.ofEpochMilli(Files.getLastModifiedTime(rightPath).toMillis())
        } else {
            null
        }

        return DiffRow(
            type = type,
            code = code,
            leftPath = leftPath,
            rightPath = rightPath,
            leftSet = leftSet,
            rightSet = rightSet,
            status = status,
            lastSyncDate = lastSyncDate
        )
    }
}