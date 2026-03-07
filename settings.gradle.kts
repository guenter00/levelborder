rootProject.name = "levelborder"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        gradlePluginPortal()
    }
}

include("common", "fabric", "paper", "vanilla", "forge")