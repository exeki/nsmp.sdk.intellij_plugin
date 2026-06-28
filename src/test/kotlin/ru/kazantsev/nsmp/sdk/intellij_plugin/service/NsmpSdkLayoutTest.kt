package ru.kazantsev.nsmp.sdk.intellij_plugin.service

import kotlin.test.Test
import kotlin.test.assertEquals

class NsmpSdkLayoutTest {

    @Test
    fun indexExcludedSubdirsDoNotIncludeGeneratedMetaclasses() {
        assertEquals(
            listOf("metainfo", "remote_state", "last_synced_state"),
            NsmpSdkLayout.INDEX_EXCLUDED_SUBDIRS,
        )
    }
}
