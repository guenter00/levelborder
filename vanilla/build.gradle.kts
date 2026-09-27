plugins {
    id("java")
    id("net.fabricmc.fabric-loom")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")

    compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
    compileOnly(project(":common"))
}