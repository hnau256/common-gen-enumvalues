package hnau.common.gen.enumvalues.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EnumValues(
    val serializable: Boolean = defaultSerializable,
) {

    companion object {

        const val defaultSerializable: Boolean = false
    }
}