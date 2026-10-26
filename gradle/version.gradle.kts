import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.bundling.Jar

val minecraftVersion = extra["minecraft_version"].toString()
val loaderVersion = rootProject.providers.gradleProperty("loader_version").get()
val javaVersion = extra["java_version"].toString().toInt()
val archiveName = providers.gradleProperty("archives_base_name").get()

configure<BasePluginExtension> {
    archivesName.set("$archiveName-mc$minecraftVersion")
}
configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}

repositories { mavenCentral() }

// Use a project dependency for compilation and unpack common into the mod JAR.
val commonBundle = configurations.create("commonBundle") { isTransitive = false }
val commonSources = configurations.create("commonSources") { isTransitive = false }
dependencies {
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    add("implementation", "net.fabricmc:fabric-loader:$loaderVersion")
    add("implementation", project(":common"))
    add("commonBundle", project(":common"))
    add("commonSources", project(mapOf("path" to ":common", "configuration" to "sourcesElements")))
    val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:2.4.20"
    add("implementation", stdlib)
    add("include", stdlib)
}

tasks.named<ProcessResources>("processResources") {
    val properties = mapOf(
        "version" to project.version.toString(),
        "minecraft_version" to minecraftVersion,
        "loader_version" to loaderVersion,
        "java_version" to javaVersion.toString()
    )
    inputs.properties(properties)
    filteringCharset = "UTF-8"
    filesMatching(listOf("fabric.mod.json", "reverser.mixins.json")) { expand(properties) }
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}
tasks.named<Jar>("jar") {
    dependsOn(commonBundle)
    from({ commonBundle.map { zipTree(it) } }) { exclude("META-INF/MANIFEST.MF") }
    from(rootProject.file("LICENSE.txt")) { rename { "LICENSE_${archiveName}" } }
}
tasks.named<Jar>("sourcesJar") {
    dependsOn(commonSources)
    from({ commonSources.map { zipTree(it) } }) { exclude("META-INF/MANIFEST.MF") }
}
configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "$archiveName-mc$minecraftVersion"
            from(components["java"])
            // Common is already embedded; it is not a separately installed mod.
            pom.withXml {
                val dependencies = asNode().get("dependencies") as groovy.util.NodeList
                dependencies.filterIsInstance<groovy.util.Node>().forEach { node ->
                    (node.children().filterIsInstance<groovy.util.Node>()).filter {
                        (it.get("artifactId") as groovy.util.NodeList).text() == "common"
                    }.forEach { node.remove(it) }
                }
            }
        }
    }
}

// Publish the POM above rather than metadata referring to the embedded common module.
tasks.withType<org.gradle.api.publish.tasks.GenerateModuleMetadata>().configureEach {
    enabled = false
}
