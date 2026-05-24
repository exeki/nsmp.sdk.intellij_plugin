package ru.kazantsev.nsmp.sdk.intellij_plugin.topics

import com.intellij.util.messages.Topic

interface ServerStateTopic {
    companion object {
        @JvmField
        val TOPIC: Topic<ServerStateTopic> = Topic.create(
            "NSMP SDK Server State Events",
            ServerStateTopic::class.java
        )
    }

    fun serverStarted(port : Int) {}

    fun serverStopped() {}

    fun serverStartError(e : Exception) {}
}