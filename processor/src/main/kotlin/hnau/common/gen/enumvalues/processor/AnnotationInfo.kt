package hnau.common.gen.enumvalues.processor

import hnau.common.gen.enumvalues.annotations.EnumValues
import kotlin.reflect.KClass

internal object AnnotationInfo {

    private val annotationClass: KClass<EnumValues> = EnumValues::class

    val nameWithPackage: String
        get() = annotationClass.qualifiedName!!

    val simpleName: String
        get() = annotationClass.simpleName!!
}