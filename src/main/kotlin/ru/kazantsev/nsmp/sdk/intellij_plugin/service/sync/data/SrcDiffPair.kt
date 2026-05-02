package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data

import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcPair
import java.nio.file.Files

class SrcDiffPair(
    override val code: String,
    override val left: SrcVirtualFile?,
    override val right: SrcVirtualFile?
) : ISrcPair<SrcVirtualFile, SrcVirtualFile> {
    val diffStatus: DiffStatus = calcDiffStatus()

    private fun calcDiffStatus(): DiffStatus {
        if (left == null && right != null) return DiffStatus.ADDED
        if (left != null && right == null) return DiffStatus.DELETED
        if (left != null && right != null) {
            val leftPath = left.virtualFile.toNioPath()
            val rightPath = right.virtualFile.toNioPath()
            return if (Files.mismatch(leftPath, rightPath) != -1L) DiffStatus.MODIFIED
            else DiffStatus.UNCHANGED
        } else throw RuntimeException("SrcDiffPair: Left and Right are null.")
    }
}