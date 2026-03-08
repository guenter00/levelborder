plugins {
    id("java")
    id("fabric-loom") version "1.15-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.140" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19" apply false
}

val MINECRAFT_VERSION by extra { "1.21.8" }
val NEOFORGE_VERSION by extra { "21.8.52" }
val FABRIC_LOADER_VERSION by extra { "0.18.4" }
val FABRIC_API_VERSION by extra { "0.136.1+1.21.8" }
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

    java.toolchain.languageVersion = JavaLanguageVersion.of(21)

    group = "de.guenter"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }
}