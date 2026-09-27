plugins {
    id("net.fabricmc.fabric-loom")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val FABRIC_LOADER_VERSION = rootProject.extra["FABRIC_LOADER_VERSION"] as String
val FABRIC_API_VERSION = rootProject.extra["FABRIC_API_VERSION"] as String
val MOD_VERSION = rootProject.extra["MOD_VERSION"] as String

base {
    archivesName.set("levelborder-fabric")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    implementation("net.fabricmc.fabric-api:fabric-api:$FABRIC_API_VERSION")

    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")

    compileOnly(project(":common"))
    compileOnly(project(":vanilla"))
}

loom {
    runs {
        named("client") {
            client()
            displayName.set("Fabric Client")
            generateRunConfig.set(true)
            runDirectory.set(file("run"))
        }
        named("server") {
            server()
            displayName.set("Fabric Server")
            generateRunConfig.set(true)
            runDirectory.set(file("run"))
        }
    }
}

tasks.withType<ProcessResources>().configureEach {
    from(project(":common").sourceSets.main.get().resources)
    from(project(":vanilla").sourceSets.main.get().resources)
    filesMatching("fabric.mod.json") {
        expand(
            "version" to MOD_VERSION,
            "minecraft_version" to MINECRAFT_VERSION.substringBefore("-").split(".").take(2).joinToString(".")
        )
    }
}

tasks.withType<Jar>().configureEach {
    from(sourceSets.main.get().output)
    from(project(":common").sourceSets.main.get().output)
    from(project(":vanilla").sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveVersion.set(MOD_VERSION)
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.withType<JavaCompile>().configureEach {
    source(project(":common").sourceSets.main.get().allSource)
    source(project(":vanilla").sourceSets.main.get().allSource)
}

tasks.withType<Test>().configureEach {
    enabled = false
}

tasks.compileTestJava {
    enabled = false
}