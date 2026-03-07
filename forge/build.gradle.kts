plugins {
    id("dev.architectury.loom")
}

val MINECRAFT_VERSION: String by rootProject.extra
val FORGE_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

repositories {
    mavenCentral()
}

base {
    archivesName.set("levelborder-forge")
}

dependencies {
    forge("net.minecraftforge:forge:$FORGE_VERSION")

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
            configName = "forge - Client"
            programArgs("--username=Dev")
            ideConfigGenerated(true)
            runDir("run")
        }
        named("server") {
            server()
            configName = "forge - Server"
            ideConfigGenerated(true)
            runDir("run")
        }
    }

    forge {
        convertAccessWideners = true
        mixinConfigs("levelborder.mixins.json")
    }
}

tasks.withType<ProcessResources>().configureEach {
    from(project(":common").sourceSets.main.get().resources)
    from(project(":vanilla").sourceSets.main.get().resources)
    filesMatching("META-INF/mods.toml") {
        expand(
            "version" to MOD_VERSION,
            "minecraft_version" to MINECRAFT_VERSION,
            "forge_version" to FORGE_VERSION
        )
    }
}

tasks.withType<Jar>().configureEach {
    from(sourceSets.main.get().output)
    from(project(":common").sourceSets.main.get().output)
    from(project(":vanilla").sourceSets.main.get().output) {
        exclude("**/LevelBorderMod.*")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.remapJar {
    archiveVersion.set(MOD_VERSION)
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.withType<JavaCompile>().configureEach {
    source(project(":common").sourceSets.main.get().allSource)
    source(project(":vanilla").sourceSets.main.get().allSource.matching {
        exclude("**/LevelBorderMod.*")
    })
}

tasks.withType<Test>().configureEach {
    enabled = false
}

tasks.compileTestJava {
    enabled = false
}