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
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
    maven("https://maven.su5ed.dev/releases") // forgified-fabric-api
}

dependencies {

    // Loader
    neoForge("net.neoforged:neoforge:${rootProject.properties["neoforge_version"] as String}") // NeoForge

    // Platform
    modImplementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:${rootProject.properties["forgified_fabric_api_version"] as String}") // Forgified Fabric API
    modImplementation("dev.architectury:architectury-neoforge:${rootProject.properties["architectury_api_version"] as String}") // Architectury API

    // Module
    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false } // common
    "shadowBundle"(project(path = ":common", configuration = "transformProductionNeoForge")) // common shadow
    "common"(project(path = ":mirrg.kotlin")) // mirrg.kotlin
    "shadowBundle"(project(path = ":mirrg.kotlin")) { isTransitive = false } // mirrg.kotlin shadow

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
