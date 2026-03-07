plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT" apply false
    id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
}

val MINECRAFT_VERSION by extra { "1.19.2" }
val FORGE_VERSION by extra { "1.19.2-43.5.2" }
val FABRIC_LOADER_VERSION by extra { "0.18.4" }
val FABRIC_API_VERSION by extra { "0.77.0+1.19.2" }
val MOD_VERSION by extra { "0.1-mc" + MINECRAFT_VERSION }

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    enabled = false
}

subprojects {
    apply(plugin = "maven-publish")

    java.toolchain.languageVersion = JavaLanguageVersion.of(17)

    group = "de.guenter"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }
}