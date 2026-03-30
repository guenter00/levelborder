rootProject.name = "levelborder"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri(rootDir.resolve("mavenLocal")) } // maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }
}

include("common", "fabric", "paper", "vanilla", "neoforge")