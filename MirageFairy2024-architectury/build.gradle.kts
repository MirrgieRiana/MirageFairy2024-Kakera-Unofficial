import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    kotlin("jvm") version "2.0.0" apply false
    `maven-publish`
    application
}

architectury {
    minecraft = project.findProperty("minecraft_version") as String
}

allprojects {
    group = rootProject.findProperty("maven_group") as String
    version = rootProject.findProperty("mod_version") as String
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    pluginManager.withPlugin("dev.architectury.loom") {
        val loom = extensions.getByType<LoomGradleExtensionAPI>()

        base {
            // Set up a suffixed format for the mod jar names, e.g. `example-fabric`.
            archivesName.set("${rootProject.findProperty("archives_name") as String}-${project.name}")
        }

        repositories {
            // Add repositories to retrieve artifacts from in here.
            // You should only use this when depending on other mods because
            // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
            // See https://docs.gradle.org/current/userguide/declaring_repositories.html
            // for more information about repositories.
        }

        extensions.configure<LoomGradleExtensionAPI> {
            silentMojangMappingsLicense()
        }

        dependencies {
            "minecraft"("net.minecraft:minecraft:${rootProject.findProperty("minecraft_version") as String}")
            "mappings"(loom.officialMojangMappings())
        }

        java {
            // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
            // if it is present.
            // If you remove this line, sources will not be generated.
            withSourcesJar()

            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(21)
        }

        // Configure Maven publishing.
        publishing {
            publications {
                create<MavenPublication>("mavenJava") {
                    artifactId = project.base.archivesName.get()
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
    }
}

tasks.register("showSourceSets") {
    doLast {
        allprojects.forEach { project ->
            println("# Project: '${project.name}'")
            project.sourceSets.asMap.forEach { (sourceSetName, sourceSet) ->
                println("## Source Set: '$sourceSetName'")
                sourceSet.allSource.srcDirs.forEach { file ->
                    println("Src: $file")
                }
                sourceSet.compileClasspath.forEach { file ->
                    if (file.isDirectory) println("Classpath: $file")
                }
            }
        }
    }
}
