plugins {
    id("java")
    id("net.fabricmc.fabric-loom")
}

val MINECRAFT_VERSION: String by rootProject.extra

repositories {
    maven("https://repo.spongepowered.org/maven/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")

    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly(project(":common"))
}