package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data

import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc

class SrcVirtualFile (
    override val code: String,
    val virtualFile : VirtualFile,
) : ISrc