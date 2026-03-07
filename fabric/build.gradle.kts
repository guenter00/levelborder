plugins {
    id("dev.architectury.loom")
}

val MINECRAFT_VERSION: String by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

repositories {
    mavenCentral()
}

base {
    archivesName.set("levelborder-fabric")
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${FABRIC_LOADER_VERSION}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${FABRIC_API_VERSION}")

    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    mappings(loom.layered { officialMojangMappings() })

    compileOnly(project(":common"))
    compileOnly(project(":vanilla"))
}

loom {
    mixin{
        defaultRefmapName.set("levelborder.refmap.json")
    }

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
            "minecraft_version" to MINECRAFT_VERSION
        )
    }
}

tasks.withType<Jar>().configureEach {
    from(sourceSets.main.get().output)
    from(project(":common").sourceSets.main.get().output)
    from(project(":vanilla").sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.remapJar {
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