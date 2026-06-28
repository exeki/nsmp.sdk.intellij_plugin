package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import javax.lang.model.element.Modifier

/**
 * Строит JavaPoet-прототип абстрактного класса-заглушки для одного метакласса.
 */
class MetaClassTypeSpecFactory(
    private val registry: MetaClassRegistry,
) {

    private val fieldFactory = AttributeFieldSpecFactory(registry)

    fun create(metaClass: MetaClassDto): TypeSpec {
        val classBuilder = TypeSpec
            .classBuilder(MetaClassNaming.classNameFromMetaCode(metaClass.fullCode))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addSuperinterface(PlatformTypes.iScriptDtObject)
            .addJavadoc(buildClassJavadoc(metaClass).build())

        if (metaClass.fullCode == "abstractBO") {
            classBuilder
                .addSuperinterface(PlatformTypes.iProperties)
                .addSuperinterface(
                    ParameterizedTypeName.get(
                        PlatformTypes.map,
                        PlatformTypes.string,
                        PlatformTypes.objectType,
                    ),
                )
        }

        metaClass.parent
            ?.takeIf(registry::contains)
            ?.let { parentCode -> classBuilder.superclass(registry.metaClassType(parentCode)) }

        registry.ownAttributes(metaClass).forEach { attribute ->
            fieldFactory.create(attribute, metaClass)?.let { field ->
                classBuilder.addField(field)
                classBuilder.addMethod(createGetter(field))
            }
        }
        return classBuilder.build()
    }

    private fun createGetter(field: FieldSpec): MethodSpec =
        MethodSpec.methodBuilder(getterName(field.name))
            .addModifiers(Modifier.PUBLIC)
            .returns(field.type)
            .addStatement("return \$N", field.name)
            .build()

    private fun getterName(fieldName: String): String =
        "get${fieldName.replaceFirstChar { char -> char.uppercaseChar() }}"

    private fun buildClassJavadoc(metaClass: MetaClassDto): CodeBlock.Builder {
        val delimiter = MetaClassGenerationSettings.CLASS_DELIMITER
        val builder = CodeBlock.builder()
            .add("<strong>Наименование: </strong>${MetaClassJavadoc.escapeHtml(metaClass.title)};<br>\n")
            .add("<strong>Код: </strong>${metaClass.fullCode.replace('$', delimiter)};<br>\n")
            .add("<strong>Жизненный цикл: </strong>${metaClass.hasWorkflow};<br>\n")
            .add("<strong>Назначение ответственного: </strong>${metaClass.hasResponsible};<br>\n")
            .add("<strong>Системный: </strong>${metaClass.hardcoded};<br>\n")
        if (metaClass.parent != null) {
            builder.add(
                "<strong>Родитель: </strong>${metaClass.parent.replace('$', delimiter)};<br>\n",
            )
        }
        MetaClassJavadoc.plainDescription(metaClass.description)?.let { description ->
            builder.add("<strong>Описание: </strong>${MetaClassJavadoc.escapeHtml(description)};")
        }
        return builder
    }
}
