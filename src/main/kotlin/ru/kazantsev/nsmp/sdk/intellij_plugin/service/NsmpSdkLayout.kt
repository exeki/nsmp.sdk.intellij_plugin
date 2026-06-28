package ru.kazantsev.nsmp.sdk.intellij_plugin.service

/**
 * Пути внутри `.nsmp_sdk` и правила индексации IDE.
 */
object NsmpSdkLayout {

    const val METAINFO_DIR = "metainfo"
    const val REMOTE_STATE_DIR = "remote_state"
    const val LAST_SYNCED_STATE_DIR = "last_synced_state"

    /** Подпапки `.nsmp_sdk`, которые не должны попадать в индекс IDE. */
    val INDEX_EXCLUDED_SUBDIRS: List<String> = listOf(
        METAINFO_DIR,
        REMOTE_STATE_DIR,
        LAST_SYNCED_STATE_DIR,
    )
}
