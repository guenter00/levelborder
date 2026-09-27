plugins {
    id("java")
    id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24" apply false
}

extra.set("MINECRAFT_VERSION", "26.3")
extra.set("NEOFORGE_VERSION", "26.3.0.26-beta")
extra.set("FABRIC_LOADER_VERSION", "0.19.5")
extra.set("FABRIC_API_VERSION", "0.161.0+26.3")
extra.set("MOD_VERSION", "0.2-mc" + (extra["MINECRAFT_VERSION"] as String))

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