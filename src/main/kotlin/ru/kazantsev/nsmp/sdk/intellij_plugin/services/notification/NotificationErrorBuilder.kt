package ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification

interface NotificationErrorBuilder {
    companion object {

        fun buildErrorText(error: Throwable): String {
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