package ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.notification

import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.LogicException
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

interface NotificationUtils {
    companion object {

        fun buildTitle(title: String): String {
            return "${MessageBundle.message("settings.display.name")}: $title"
        }

        fun buildErrorText(error: Throwable): String {
            if (error is LogicException) return error.message
            val parts = mutableListOf<String>()
            parts.add(buildErrorPart(error))
            var cause = error.cause
            while (cause != null) {
                parts.add(buildErrorPart(error))
                cause = cause.cause
            }
            return parts.joinToString(separator = "\nCaused by: ")
        }

        fun buildErrorText(message: String, error: Throwable): String {
            if (error is LogicException) return message + "\n" + error.message
            val parts = mutableListOf<String>()
            parts.add(buildErrorPart(error))
            var cause = error.cause
            while (cause != null) {
                parts.add(buildErrorPart(error))
                cause = cause.cause
            }
            return message + "\n" + parts.joinToString(separator = "\nCaused by: ")
        }

        fun buildErrorPart(error: Throwable): String {
            return "${error.javaClass.simpleName}: ${error.message ?: error.toString()}"
        }

    }
}