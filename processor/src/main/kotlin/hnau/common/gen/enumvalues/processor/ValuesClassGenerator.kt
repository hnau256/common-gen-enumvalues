package hnau.common.gen.enumvalues.processor

import arrow.core.NonEmptyList
import hnau.common.gen.kt.Importable
import hnau.common.gen.kt.generateKt
import hnau.common.gen.kt.inject

fun generateEnumValuesClass(
    pkg: String,
    enumClassName: String,
    enumValuesClassName: String,
    entries: NonEmptyList<String>,
    serializable: Boolean,
): String = generateKt(
    pkg = pkg,
) {
    val enumPropertyName = enumClassName.replaceFirstChar(Char::lowercase)
    val items = entries.withPropertiesNames(
        enumClassName = enumClassName,
    )
    if (serializable) {
        +"@${inject(Importables.serializable)}"
    }
    +"data class $enumValuesClassName<out T>("
    indent {
        items.forEach { (_, property) ->
            +"val $property: T,"
        }
    }
    +") {"
    indent {
        +""
        +"operator fun get("
        indent {
            +"$enumPropertyName: $enumClassName,"
        }
        +"): T = when (part) {"
        indent {
            items.forEach { (entry, property) ->
                +"$entry -> $property"
            }
        }
        +"}"
        +""
        +"inline fun <R> map("
        indent {
            +"transform: ($enumPropertyName: $enumClassName, value: T) -> R,"
        }
        +"): $enumValuesClassName<R> = $enumValuesClassName("
        indent {
            items.forEach { (entry, property) ->
                +"$property = transform($entry, $property),"
            }
        }
        +")"
        +""
        +"inline fun <R> map("
        indent {
            +"transform: (value: T) -> R,"
        }
        +"): $enumValuesClassName<R> = map { _, value ->"
        indent {
            +"transform(value)"
        }
        +"}"
        +""
        +"inline fun <O, R> combineWith("
        indent {
            +"other: $enumValuesClassName<O>,"
            +"combine: ($enumPropertyName: $enumClassName, value: T, other: O) -> R,"
        }
        +"): $enumValuesClassName<R> = $enumValuesClassName("
        indent {
            items.forEach { (entry, property) ->
                +"$property = combine($entry, $property, other.$property),"
            }
        }
        +")"
        +""
        +"inline fun <O, R> combineWith("
        indent {
            +"other: $enumValuesClassName<O>,"
            +"combine: (value: T, other: O) -> R,"
        }
        +"): $enumValuesClassName<R> = combineWith("
        indent {
            +"other = other,"
        }
        +") { _, value, other ->"
        indent {
            +"combine(value, other)"
        }
        +"}"
    }
    +"}"
}

private data class EntryWithPropertyName(
    val entry: String,
    val property: String,
)

private fun NonEmptyList<String>.withPropertiesNames(
    enumClassName: String,
): NonEmptyList<EntryWithPropertyName> = map { entryName ->
    EntryWithPropertyName(
        entry = "$enumClassName.$entryName",
        property = entryName.replaceFirstChar(Char::lowercase)
    )
}

private object Importables {
    val serializable = Importable("kotlinx.serialization", "Serializable")
}