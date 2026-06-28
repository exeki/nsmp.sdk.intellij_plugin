package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import ru.kazantsev.nsmp.sdk.intellij_plugin.codegen.MetaClassGenerationSettings

/**
 * Сбрасывает кэш nsmp:meta-привязок при правке скриптов и перегенерации заглушек.
 */
@Service(Service.Level.PROJECT)
class MetaClassBindingInvalidationService(private val project: Project) : Disposable {

    private val cache = project.service<MetaClassBindingTypeCache>()
    private val busConnection = project.messageBus.connect(this)
    private val generatedStubPathSegment =
        "/${MetaClassGenerationSettings.GENERATED_SOURCES_RELATIVE_PATH.replace('\\', '/')}/"

    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            val file = FileDocumentManager.getInstance().getFile(event.document) ?: return
            if (!file.path.endsWith(".groovy", ignoreCase = true)) return
            cache.clearForFile(file.path)
        }
    }

    init {
        busConnection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                if (events.any(::isGeneratedStubChange)) {
                    cache.clear()
                }
            }
        })
        com.intellij.openapi.editor.EditorFactory.getInstance()
            .eventMulticaster
            .addDocumentListener(documentListener, this)
    }

    private fun isGeneratedStubChange(event: VFileEvent): Boolean {
        val path = event.path.replace('\\', '/')
        return path.contains(generatedStubPathSegment)
    }

    override fun dispose() {
        busConnection.disconnect()
    }
}
