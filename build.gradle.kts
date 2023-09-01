plugins {
    kotlin("jvm") version "1.9.10"
    alias(libs.plugins.axion.release)
}

group = "pl.north93"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)
    implementation(libs.jopt.simple)
    implementation(libs.github.api)
    implementation(libs.jgit)
    implementation(libs.javassist)

    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml)
    implementation(libs.jackson.kotlin)

    testImplementation(kotlin("test"))
}

tasks.jar {
    manifest.attributes["Main-Class"] = "pl.north93.pullrequest.automerge.Main"
    manifest.attributes["Multi-Release"] = true
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    exclude("**/*.RSA", "**/*.SF")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}