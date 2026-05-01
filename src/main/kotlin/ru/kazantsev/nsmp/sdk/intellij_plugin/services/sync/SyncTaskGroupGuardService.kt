package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.intellij.openapi.components.Service
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.SyncTaskAlreadyRunningException
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.PROJECT)
class SyncTaskGroupGuardService {
    private val busy = AtomicBoolean(false)

    fun acquireOrThrow() {
        if (!busy.compareAndSet(false, true)) throw SyncTaskAlreadyRunningException()
    }

    fun release() {
        busy.set(false)
    }
}
