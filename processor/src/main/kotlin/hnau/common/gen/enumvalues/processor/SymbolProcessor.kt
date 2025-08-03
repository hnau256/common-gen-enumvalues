package hnau.common.gen.enumvalues.processor

import arrow.core.toNonEmptyListOrNull
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import hnau.common.kotlin.castOrNull
import hnau.common.gen.enumvalues.annotations.EnumValues
import java.io.OutputStreamWriter

class SymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(
        resolver: Resolver,
    ): List<KSAnnotated> {

        resolver
            .getSymbolsWithAnnotation(AnnotationInfo.nameWithPackage)
            .forEach { annotated -> processAnnotated(annotated) }

        return emptyList()
    }

    private fun processAnnotated(
        annotated: KSAnnotated,
    ) {

        if (!annotated.validate()) {
            logger.warn("${annotated.location} is not valid")
            return
        }

        val annotationLog = "@${AnnotationInfo.simpleName}"

        val classDeclaration = annotated as? KSClassDeclaration
        if (classDeclaration == null) {
            logger.error("$annotationLog can only be applied to classes", annotated)
            return
        }

        if (classDeclaration.classKind != ClassKind.ENUM_CLASS) {
            logger.error("$annotationLog can only be applied to enum classes", classDeclaration)
            return
        }

        val enumEntries = classDeclaration
            .declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_ENTRY }
            .map { it.simpleName.asString() }
            .toList()
            .toNonEmptyListOrNull()

        if (enumEntries == null) {
            logger.error("Enum has no entries", classDeclaration)
            return
        }

        val annotation = classDeclaration
            .annotations
            .first { it.shortName.asString() == AnnotationInfo.simpleName }

        val serializable = annotation
            .arguments
            .find { it.name?.asString() == "serializable" }
            ?.value
            .castOrNull<Boolean>()
            ?: EnumValues.defaultSerializable

        val packageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName?.asString()
                ?: classDeclaration.simpleName.asString()
        val enumClassName = qualifiedName.removePrefix("$packageName.")
        val flattenEnumClassName = enumClassName.replace(".", "")
        val generatedClassName = "${flattenEnumClassName}Values"

        logger.info("Generating $packageName.$generatedClassName for $enumClassName")

        val generatedFileContent: String = generateEnumValuesClass(
            pkg = packageName,
            entries = enumEntries,
            enumClassName = enumClassName,
            enumValuesClassName = generatedClassName,
            serializable = serializable,
        )

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, classDeclaration.containingFile!!),
            packageName = packageName,
            fileName = generatedClassName
        )

        OutputStreamWriter(file).use { writer ->
            writer.write(generatedFileContent)
        }
    }
}