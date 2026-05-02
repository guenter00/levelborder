plugins {
    id("net.fabricmc.fabric-loom")
}

val MINECRAFT_VERSION: String by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

base {
    archivesName.set("levelborder-fabric")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.fabricmc:fabric-loader:${FABRIC_LOADER_VERSION}")
    implementation("net.fabricmc.fabric-api:fabric-api:${FABRIC_API_VERSION}")

    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")

    compileOnly(project(":common"))
    compileOnly(project(":vanilla"))
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
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