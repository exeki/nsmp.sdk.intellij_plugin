package ru.kazantsev.nsmp.sdk.intellij_plugin.topics

import com.intellij.util.messages.Topic
import ru.kazantsev.nsmp.sdk.intellij_plugin.task.backgroundable.AbstractBackgroundTask

interface SettingsTopic {
    companion object {
        @JvmField
        val TOPIC: Topic<SettingsTopic> = Topic.create(
            "NSMP SDK Settings Events",
            SettingsTopic::class.java
        )
    }

    fun settingsChanged() {}

}