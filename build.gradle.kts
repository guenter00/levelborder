plugins {
    id("java")
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
}

val MINECRAFT_VERSION by extra { "26.2" }
val NEOFORGE_VERSION by extra { "26.2.0.0-beta" }
val FABRIC_LOADER_VERSION by extra { "0.19.3" }
val FABRIC_API_VERSION by extra { "0.152.1+26.2" }
val MOD_VERSION by extra { "0.2-mc" + MINECRAFT_VERSION }

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

    java.toolchain.languageVersion = JavaLanguageVersion.of(25)

    group = "de.guenter"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }
}