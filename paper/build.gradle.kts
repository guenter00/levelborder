plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val MINECRAFT_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven { url = uri(rootDir.resolve("mavenLocal")) }
}

base {
    archivesName.set("levelborder-paper")
}

dependencies {
    paperweight.devBundle("org.purpurmc.purpur:dev-bundle:$MINECRAFT_VERSION-R0.1-SNAPSHOT")

    compileOnly("org.purpurmc.purpur:purpur-api:$MINECRAFT_VERSION-R0.1-SNAPSHOT")
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
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))}

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