package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName

/**
 * FQN платформенных и JDK-типов, используемых при генерации полей заглушек.
 */
object PlatformTypes {
    val objectType: ClassName = ClassName.get("java.lang", "Object")
    val string: ClassName = ClassName.get("java.lang", "String")
    val longBoxed: ClassName = ClassName.get("java.lang", "Long")
    val doubleBoxed: ClassName = ClassName.get("java.lang", "Double")
    val booleanBoxed: ClassName = ClassName.get("java.lang", "Boolean")
    val byteArray: TypeName = ArrayTypeName.of(TypeName.BYTE)

    val scriptDtOList: ClassName = ClassName.get("ru.naumen.core.server.script.spi", "ScriptDtOList")
    val scriptDtOSet: ClassName = ClassName.get("ru.naumen.core.server.script.spi", "ScriptDtOSet")
    val scriptDate: ClassName = ClassName.get("ru.naumen.core.server.script.spi", "ScriptDate")
    val aggregateContainerWrapper: ClassName =
        ClassName.get("ru.naumen.core.server.script.spi", "AggregateContainerWrapper")
    val iScriptDtObject: ClassName = ClassName.get("ru.naumen.core.server.script.spi", "IScriptDtObject")
    val iProperties: ClassName = ClassName.get("ru.naumen.common.shared.utils", "ISProperties")
    val map: ClassName = ClassName.get("java.util", "Map")

    val dateTimeInterval: ClassName = ClassName.get("ru.naumen.common.shared.utils", "DateTimeInterval")
    val iHyperlink: ClassName = ClassName.get("ru.naumen.common.shared.utils", "IHyperlink")
    val sourceCode: ClassName = ClassName.get("ru.naumen.common.shared.utils", "SourceCode")
    val backTimerDto: ClassName = ClassName.get("ru.naumen.core.shared.timer", "BackTimerDto")
    val timerDto: ClassName = ClassName.get("ru.naumen.core.shared.timer", "TimerDto")
    val iClassFqn: ClassName = ClassName.get("ru.naumen.metainfo.shared", "IClassFqn")
    val iSGroup: ClassName = ClassName.get("ru.naumen.metainfo.shared.elements.sec", "ISGroup")
}
