extra["minecraft_version"] = "26.3"
extra["java_version"] = 25

apply(from = rootProject.file("gradle/version.gradle.kts"))

dependencies {
    implementation("net.fabricmc.fabric-api:fabric-api:0.160.6+26.3")
}
