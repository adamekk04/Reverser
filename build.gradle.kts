plugins {
    base
    kotlin("jvm") version "2.4.20" apply false
    id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT" apply false
}

allprojects {
    version = providers.gradleProperty("mod_version").get()
    group = providers.gradleProperty("maven_group").get()
}

// Apply plugins before evaluating each version's small build file.
subprojects {
    if (projectDir.parentFile.name == "versions") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "net.fabricmc.fabric-loom")
        apply(plugin = "maven-publish")
    }
}

val versionProjects = subprojects.filter { it.projectDir.parentFile.name == "versions" }
tasks.named("build") { dependsOn(versionProjects.map { "${it.path}:build" }) }
tasks.named("clean") { dependsOn(subprojects.map { "${it.path}:clean" }) }
