package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class MetaClassGenerationSettingsTest {

    @Test
    fun generatedMetaclassesPathIsUnderNsmpSdk() {
        assertEquals(".nsmp_sdk/generated/metaclasses", MetaClassGenerationSettings.GENERATED_SOURCES_RELATIVE_PATH)
    }
}
