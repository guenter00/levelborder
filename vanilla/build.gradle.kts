plugins {
    id("java")
    id("dev.architectury.loom")
}

val MINECRAFT_VERSION: String by rootProject.extra

repositories {
    maven("https://repo.spongepowered.org/maven/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    mappings(loom.layered { officialMojangMappings() })

    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly(project(":common"))
}

loom {
    @Suppress("UnstableApiUsage")
    mixin.useLegacyMixinAp.set(false)
}