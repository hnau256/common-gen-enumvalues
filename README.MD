# EnumValues KSP Generator

A minimalist Kotlin Symbol Processing (KSP) code generator that creates type-safe, compiler-verified data classes for associating values with enum entries, ensuring each enum value has a corresponding data entry.

## Features

- **Type Safety**: Generates data classes that guarantee each enum entry has an associated value, preventing runtime errors due to missing mappings.
- **Compiler-Verified**: Ensures correctness at compile time, reducing the need for runtime checks.
- **Serializable Support**: Optionally generates classes with `kotlinx.serialization.Serializable` annotation for seamless serialization.
- **Utility Functions**: Includes additional functions like `map`, `combineWith`, and `create` for flexible value transformations and combinations.
- **Lightweight**: Minimalistic design focused on simplicity and reliability.

## Example

Given an enum:

```kotlin
enum class Side {
    Left,
    Right,
}
```

Annotate it with `@EnumValues`:

```kotlin
@EnumValues(
    serializable = true,
)
enum class Side {
    Left,
    Right,
}
```

The generator produces a data class like:

```kotlin
@Serializable
data class SideValues<out T>(
    val left: T,
    val right: T,
) {

    operator fun get(
        side: Side,
    ): T = when (side) {
        Side.Left -> left
        Side.Right -> right
    }

    inline fun <R> map(
        transform: (side: Side, value: T) -> R,
    ): SideValues<R> = SideValues(
        left = transform(Side.Left, left),
        right = transform(Side.Right, right),
    )

    inline fun <R> map(
        transform: (value: T) -> R,
    ): SideValues<R> = map { _, value ->
        transform(value)
    }

    inline fun <O, R> combineWith(
        other: SideValues<O>,
        combine: (side: Side, value: T, other: O) -> R,
    ): SideValues<R> = SideValues(
        left = combine(Side.Left, left, other.left),
        right = combine(Side.Right, right, other.right),
    )

    inline fun <O, R> combineWith(
        other: SideValues<O>,
        combine: (value: T, other: O) -> R,
    ): SideValues<R> = combineWith(
        other = other,
    ) { _, value, other ->
        combine(value, other)
    }

    companion object {

        inline fun <T> create(
            createValue: (side: Side) -> T,
        ): SideValues<T> = SideValues(
            left = createValue(Side.Left),
            right = createValue(Side.Right),
        )
    }
}
```

With `serializable = true` in the `@EnumValues` annotation, the generated class includes the `@Serializable` annotation for use with `kotlinx.serialization`.

## Setup

### Prerequisites

- Kotlin
- Gradle
- Kotlin Symbol Processing (KSP)
- Optional: `kotlinx.serialization` for serialization support

### Adding the Dependency

This library is available via JitPack. Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.hnau256.common-gen-enumvalues:annotations:1.0.0")
    ksp("com.github.hnau256.common-gen-enumvalues:processor:1.0.0")
}
```

For KSP configuration in a multiplatform project, add:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.enumvalues.annotations)
            }
        }
        commonMain {
            kotlin.srcDir("build/generated/ksp/commonMain/kotlin")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.enumvalues.processor)
}
```

In your `gradle.properties`, ensure you have:

```properties
org.gradle.jvmargs=-Xmx4g
```

### Usage

1. Add the `@EnumValues` annotation to your enum class:

```kotlin
import hnau.common.gen.enumvalues.annotations.EnumValues

@EnumValues(serializable = true)
enum class Side {
    Left,
    Right,
}
```

2. Run the Gradle build to generate the corresponding `SideValues` class.
3. Use the generated class in your code to associate values with enum entries in a type-safe manner.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.