package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.AttributeDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.AttributeType
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import javax.lang.model.element.Modifier

/**
 * Строит JavaPoet-прототипы полей метакласса на основе типа атрибута NSMP.
 */
class AttributeFieldSpecFactory(
    private val registry: MetaClassRegistry,
) {

    fun create(attribute: AttributeDto, metaClass: MetaClassDto): FieldSpec? {
        if (attribute.code in FORBIDDEN_FIELD_NAMES) return null

        val builder = fieldBuilder(attribute) ?: return null
        builder.initializer("null")
        if (attribute.required) builder.addAnnotation(NotNull::class.java)
        else builder.addAnnotation(Nullable::class.java)
        builder.addJavadoc(buildFieldJavadoc(attribute).build())
        return builder.build()
    }

    private fun fieldBuilder(attribute: AttributeDto): FieldSpec.Builder? =
        when (attribute.resolvedType()) {
            AttributeType.DOUBLE -> simpleField(PlatformTypes.doubleBoxed, attribute.code)
            AttributeType.AGGREGATE -> simpleField(PlatformTypes.aggregateContainerWrapper, attribute.code)
            AttributeType.BACK_BO_LINKS -> collectionField(PlatformTypes.scriptDtOList, attribute)
            AttributeType.BACK_TIMER -> simpleField(PlatformTypes.backTimerDto, attribute.code)
            AttributeType.BOOL -> simpleField(PlatformTypes.booleanBoxed, attribute.code)
            AttributeType.BO_LINKS -> collectionField(PlatformTypes.scriptDtOSet, attribute)
            AttributeType.CASE_LIST -> genericField(PlatformTypes.scriptDtOList, PlatformTypes.iClassFqn, attribute.code)
            AttributeType.CATALOG_ITEM -> metaClassField(attribute)
            AttributeType.CATALOG_ITEM_SET -> collectionField(PlatformTypes.scriptDtOSet, attribute)
            AttributeType.DATE -> simpleField(PlatformTypes.scriptDate, attribute.code)
            AttributeType.DATE_TIME -> simpleField(PlatformTypes.scriptDate, attribute.code)
            AttributeType.DT_INTERVAL -> simpleField(PlatformTypes.dateTimeInterval, attribute.code)
            AttributeType.INTEGER -> simpleField(PlatformTypes.longBoxed, attribute.code)
            AttributeType.META_CLASS -> simpleField(PlatformTypes.iClassFqn, attribute.code)
            AttributeType.OBJECT -> metaClassField(attribute)
            AttributeType.STATE -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.RESPONSIBLE -> simpleField(PlatformTypes.aggregateContainerWrapper, attribute.code)
            AttributeType.STRING -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.TEXT -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.TIMER -> simpleField(PlatformTypes.timerDto, attribute.code)
            AttributeType.HYPERLINK -> simpleField(PlatformTypes.iHyperlink, attribute.code)
            AttributeType.RICH_TEXT -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.SEC_GROUPS -> genericField(PlatformTypes.scriptDtOList, PlatformTypes.iSGroup, attribute.code)
            AttributeType.FILE -> genericField(
                PlatformTypes.scriptDtOList,
                registry.metaClassType("file"),
                attribute.code,
            )
            AttributeType.LICENSE -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.FILE_CONTENT -> simpleField(PlatformTypes.byteArray, attribute.code)
            AttributeType.UUID -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.COLOR -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.LOCALIZED_TEXT -> simpleField(PlatformTypes.string, attribute.code)
            AttributeType.SOURCE_CODE -> simpleField(PlatformTypes.sourceCode, attribute.code)
            AttributeType.UNKNOWN,
            AttributeType.COMMENT_OBJECTS,
            AttributeType.RECORD_TYPE,
            AttributeType.MULTI_CLASS_OBJECTS,
            AttributeType.SYSTEM_OBJECT,
            AttributeType.SYSTEM_STATE,
            AttributeType.LINKED_CLASSES,
            AttributeType.METRIC_SEVERITY,
            AttributeType.METRIC_VALUE,
            AttributeType.EXECUTION_RESULT,
            -> simpleField(PlatformTypes.objectType, attribute.code)
        }

    private fun simpleField(type: TypeName, fieldName: String): FieldSpec.Builder =
        FieldSpec.builder(type, fieldName, Modifier.PUBLIC, Modifier.FINAL)

    private fun genericField(container: ClassName, generic: ClassName, fieldName: String): FieldSpec.Builder =
        FieldSpec.builder(
            ParameterizedTypeName.get(container, generic),
            fieldName,
            Modifier.PUBLIC,
            Modifier.FINAL,
        )

    private fun collectionField(container: ClassName, attribute: AttributeDto): FieldSpec.Builder {
        val elementType = attribute.relatedMetaClass
            ?.let { registry.metaClassType(it) }
            ?: PlatformTypes.objectType
        return genericField(container, elementType, attribute.code)
    }

    private fun metaClassField(attribute: AttributeDto): FieldSpec.Builder {
        val metaCode = attribute.relatedMetaClass ?: return simpleField(PlatformTypes.objectType, attribute.code)
        return simpleField(registry.metaClassType(metaCode), attribute.code)
    }

    private fun buildFieldJavadoc(attribute: AttributeDto): CodeBlock.Builder {
        val type = attribute.resolvedType()
        val builder = CodeBlock.builder()
            .add("<strong>Наименование: </strong>${MetaClassJavadoc.escapeHtml(attribute.title)};<br>\n")
            .add("<strong>Код: </strong>${attribute.code};<br>\n")
            .add("<strong>Тип: </strong>${type.title};<br>\n")
        if (attribute.relatedMetaClass != null) {
            builder.add("<strong>Связанный метакласс: </strong>${attribute.relatedMetaClass};<br>\n")
        }
        builder
            .add("<strong>Обязателен: </strong>${attribute.required};<br>\n")
            .add("<strong>Обязателен в интерфейсе: </strong>${attribute.requiredInInterface};<br>\n")
            .add("<strong>Уникальный: </strong>${attribute.unique};<br>\n")
            .add("<strong>Системный: </strong>${attribute.hardcoded};<br>\n")
        MetaClassJavadoc.plainDescription(attribute.description)?.let { description ->
            builder.add("<strong>Описание: </strong>${MetaClassJavadoc.escapeHtml(description)};")
        }
        return builder
    }

    private companion object {
        val FORBIDDEN_FIELD_NAMES = setOf(
            "private", "protected", "interface", "class", "int", "if", "boll",
        )
    }
}
