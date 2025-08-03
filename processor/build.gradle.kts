plugins {
    kotlin("jvm")
    id("maven-publish")
}

tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            version = project.version as String
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
        }
    }
}

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.hnau.kotlin)
    implementation(libs.hnau.gen.kt)
    implementation(libs.arrow.core)
    implementation(project(":annotations"))
}