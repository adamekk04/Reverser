plugins {
    kotlin("jvm")
    `java-library`
}

repositories { mavenCentral() }

// Keep shared logic usable by versions targeting older Java runtimes too.
kotlin {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
}
tasks.withType<JavaCompile>().configureEach { options.release.set(17) }
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}
