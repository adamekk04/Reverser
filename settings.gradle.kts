pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}

rootProject.name = "Reverser"
include(":common")

// Adding versions/<minecraft-version>/build.gradle.kts registers a new module.
file("versions").listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
    ?.sortedBy { it.name }
    ?.forEach { directory ->
        include(":${directory.name}")
        project(":${directory.name}").projectDir = directory
    }
