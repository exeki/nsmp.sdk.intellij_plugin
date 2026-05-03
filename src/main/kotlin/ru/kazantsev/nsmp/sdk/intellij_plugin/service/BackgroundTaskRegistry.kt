package ru.kazantsev.nsmp.sdk.intellij_plugin.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.AbstractBackgroundTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class BackgroundTaskRegistry(private val project: Project) {
    val activeTasks = ConcurrentHashMap<UUID, AbstractBackgroundTask>()

    fun register(task: AbstractBackgroundTask) {
        activeTasks[task.uuid] = task
        project.messageBus.syncPublisher(BackgroundTaskTopic.TOPIC).taskStarted(task)
    }

    fun unregister(task: AbstractBackgroundTask) {
        activeTasks.remove(task.uuid)
        project.messageBus.syncPublisher(BackgroundTaskTopic.TOPIC).taskFinished(task)
    }

    inline fun <reified T : Any> getByType(): List<T> {
        return activeTasks.values.filterIsInstance<T>()
    }
}
