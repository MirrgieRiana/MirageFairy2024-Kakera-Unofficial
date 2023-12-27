plugins {
    id("fabric-loom") version "1.4-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "1.9.20"
    id("com.modrinth.minotaur") version "2.+"
}

version = project.properties["mod_version"] as String
group = project.properties["maven_group"] as String

base {
    archivesName = project.properties["archives_base_name"] as String
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.

    maven("https://maven.shedaniel.me") // RoughlyEnoughItems

}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("miragefairy2024") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
    runs {
        // This adds a new gradle task that runs the datagen API: "gradlew runDatagen"
        register("datagen") {
            inherit(runs["server"])
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated")}")
            vmArg("-Dfabric-api.datagen.modid=miragefairy2024")

            runDir("build/datagen")
        }
    }
}

// Add the generated resources to the main source set
sourceSets {
    main {
        resources {
            srcDir(
                "src/main/generated"
            )
        }
    }
}
dependencies {
    // To change the versions see the gradle.properties file
    "minecraft"("com.mojang:minecraft:${project.properties["minecraft_version"] as String}")
    "mappings"("net.fabricmc:yarn:${project.properties["yarn_mappings"] as String}:v2")
    "modImplementation"("net.fabricmc:fabric-loader:${project.properties["loader_version"] as String}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"] as String}")
    "modImplementation"("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"] as String}")
    // Uncomment the following line to enable the deprecated Fabric API modules.
    // These are included in the Fabric API production distribution and allow you to update your mod to the latest modules at a later more convenient time.

    // modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:${project.fabric_version}")

    "modRuntimeOnly"("me.shedaniel:RoughlyEnoughItems-fabric:13.0.678")
    "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-api-fabric:13.0.678")
    "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:13.0.678")
    "modCompileOnly"("me.shedaniel.cloth:basic-math:0.6.1")
}

tasks.named<Copy>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "17"
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named<Jar>("jar") {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

// configure the maven publication
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}

// https://github.com/modrinth/minotaur
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "miragefairy2024"
    //versionNumber = project.mod_version
    versionType = "beta"
    uploadFile = tasks["remapJar"]
    //gameVersions = ["1.20.2"]
    //loaders = ["fabric"]
    syncBodyFrom = rootProject.file("MODRINTH-BODY.md").readText()
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
    }
}

tasks["modrinth"].dependsOn(tasks["modrinthSyncBody"])
