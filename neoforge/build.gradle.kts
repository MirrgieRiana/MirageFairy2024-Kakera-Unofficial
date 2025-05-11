import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.modrinth.minotaur")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

configurations {
    val common by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentNeoForge").extendsFrom(common)

    // Files in this configuration will be bundled into your mod using the Shadow plugin.
    // Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
    maven("https://maven.neoforged.net/releases") // NeoForged
    maven("https://maven.su5ed.dev/releases") // forgified-fabric-api
    maven("https://thedarkcolour.github.io/KotlinForForge/") // kotlin-for-forge
    maven("https://maven.shedaniel.me") // RoughlyEnoughItems
    maven("https://maven.wispforest.io/releases/") // owo-lib
}

dependencies {

    // Loader
    neoForge("net.neoforged:neoforge:${rootProject.properties["neoforge_version"] as String}") // NeoForge

    // Platform
    modImplementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:${rootProject.properties["forgified_fabric_api_version"] as String}") // Forgified Fabric API
    modImplementation("thedarkcolour:kotlinforforge:${rootProject.properties["kotlin_for_forge_version"] as String}") // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.properties["kotlin_coroutines_version"] as String}") // Kotlin Coroutines
    modImplementation("dev.architectury:architectury-neoforge:${rootProject.properties["architectury_api_version"] as String}") // Architectury API

    // Module
    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false } // common
    "shadowBundle"(project(path = ":common", configuration = "transformProductionNeoForge")) // common shadow
    "common"(project(path = ":mirrg.kotlin")) // mirrg.kotlin
    "shadowBundle"(project(path = ":mirrg.kotlin")) { isTransitive = false } // mirrg.kotlin shadow

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-neoforge:16.0.799")
    modCompileOnly("me.shedaniel.cloth:basic-math:0.6.1")

    modImplementation("io.wispforest:owo-lib-neoforge:0.12.15.1-beta.3+1.21")// { isTransitive = true }
    forgeRuntimeLibrary(include(api("io.wispforest:endec:0.1.5.1")!!)!!)
    forgeRuntimeLibrary(include(api("io.wispforest.endec:netty:0.1.2")!!)!!)
    forgeRuntimeLibrary(include(api("io.wispforest.endec:gson:0.1.3.1")!!)!!)
    forgeRuntimeLibrary(include(api("io.wispforest.endec:jankson:0.1.3.1")!!)!!)
    forgeRuntimeLibrary(include(api("blue.endless:jankson:1.2.2")!!)!!)

}

// https://github.com/modrinth/minotaur
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "miragefairy2024-kakera-unofficial"
    //versionNumber = project.mod_version
    versionType = "beta"
    uploadFile = tasks["remapJar"]
    //gameVersions = ["1.20.2"]
    //loaders = ["neoforge"]
    dependencies {
        required.project("forgified-fabric-api")
        required.project("kotlin-for-forge")
    }
}
//rootProject.tasks["uploadModrinth"].dependsOn(tasks["modrinth"])

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "version" to project.version,
            "forgified_fabric_api_version" to rootProject.properties["forgified_fabric_api_version"] as String,
            "kotlin_for_forge_version" to rootProject.properties["kotlin_for_forge_version"] as String,
        )
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations.getByName("shadowBundle"))
    archiveClassifier.set("dev-shadow")
    relocate("mirrg.kotlin", "miragefairy2024.shadow.mirrg.kotlin")
}

tasks.named<RemapJarTask>("remapJar") {
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}
