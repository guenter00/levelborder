plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val MINECRAFT_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

base {
    archivesName.set("levelborder-paper")
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("$MINECRAFT_VERSION.build.+")

    compileOnly("io.papermc.paper:paper-api:$MINECRAFT_VERSION.build.+")
    compileOnly(project(":common"))
    compileOnly(project(":vanilla"))
}

tasks.withType<ProcessResources>().configureEach {
    from(project(":common").sourceSets.main.get().resources) {
        exclude("**/*.mixins.json")
    }
    from(project(":vanilla").sourceSets.main.get().resources)
    filesMatching("plugin.yml") {
        expand(
            "version" to MOD_VERSION,
            "minecraft_version" to MINECRAFT_VERSION.substringBefore("-").split(".").take(2).joinToString(".")
        )
    }
}

tasks.withType<Jar>().configureEach {
    from(sourceSets.main.get().output)
    from(project(":common").sourceSets.main.get().output) {
        exclude("**/*.mixins.json")
    }
    from(project(":vanilla").sourceSets.main.get().output) {
        exclude("**/mixin/**")
        exclude("**/LevelBorderMod.*")
    }
    archiveVersion.set(MOD_VERSION)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.withType<JavaCompile>().configureEach {
    source(project(":common").sourceSets.main.get().allSource)
    source(project(":vanilla").sourceSets.main.get().allSource)
    exclude("**/mixin/**")
    exclude("**/LevelBorderMod.*")
}

tasks.withType<Test>().configureEach {
    enabled = false
}

tasks.compileTestJava {
    enabled = false
}