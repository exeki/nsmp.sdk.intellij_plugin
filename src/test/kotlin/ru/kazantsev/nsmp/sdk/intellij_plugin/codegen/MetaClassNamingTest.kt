package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class MetaClassNamingTest {

    @Test
    fun classNameFromSimpleMetaCode() {
        assertEquals("Employee_SDO", MetaClassNaming.classNameFromMetaCode("employee"))
    }

    @Test
    fun classNameFromCaseMetaCode() {
        assertEquals("ServiceCall_Request_SDO", MetaClassNaming.classNameFromMetaCode("serviceCall\$request"))
    }

    @Test
    fun qualifiedClassNameUsesPlatformPackage() {
        assertEquals(
            "ru.naumen.core.server.script.spi.Employee_SDO",
            MetaClassNaming.qualifiedClassName("employee"),
        )
    }
}
