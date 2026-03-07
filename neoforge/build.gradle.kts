plugins {
    id("net.neoforged.moddev")
}

val notNeoTask: (Task) -> Boolean = { !it.name.startsWith("neo") && !it.name.startsWith("compileService") }
val NEOFORGE_VERSION: String by rootProject.extra
val MINECRAFT_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

base {
    archivesName = "levelborder-neoforge"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":vanilla"))
}

neoForge {
    version = NEOFORGE_VERSION
    runs {
        create("client") {
            client()
        }
    }
    runs {
        create("server") {
            server()
        }
    }

    mods {
        create("levelborder_minecraft") {
            sourceSet(sourceSets.main.get())
        }
    }
}
tasks.withType<ProcessResources>().matching(notNeoTask).configureEach {
    from(project(":common").sourceSets.main.get().resources)
    from(project(":vanilla").sourceSets.main.get().resources)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "version" to MOD_VERSION,
            "neoforge_version" to NEOFORGE_VERSION.substringBefore("-").split(".").take(2).joinToString("."),
            "minecraft_version" to MINECRAFT_VERSION
        )
    }
}

tasks.withType<Jar>().configureEach {
    from(sourceSets.main.get().output)
    from(project(":common").sourceSets.main.get().output)
    from(project(":vanilla").sourceSets.main.get().output) {
        exclude("**/LevelBorderMod.*")
    }
    archiveVersion.set(MOD_VERSION)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.withType<JavaCompile>().matching(notNeoTask).configureEach {
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