package ru.kazantsev.nsmp.sdk.intellij_plugin.service

import com.intellij.util.messages.Topic
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.AbstractBackgroundTask

interface BackgroundTaskTopic {
    companion object {
        @JvmField
        val TOPIC: Topic<BackgroundTaskTopic> = Topic.create(
            "NSMP SDK Background Task Events",
            BackgroundTaskTopic::class.java
        )
    }

    fun taskStarted(task: AbstractBackgroundTask) {}

    fun taskFinished(task: AbstractBackgroundTask) {}
}

