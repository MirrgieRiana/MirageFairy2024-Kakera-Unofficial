plugins {
    kotlin("jvm") version "2.0.0"
    id("dev.architectury.loom") version "1.7-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org") // mapping
}

dependencies {
    "minecraft"("net.minecraft:minecraft:${rootProject.properties["minecraft_version"] as String}")
    "mappings"(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.properties["minecraft_version"] as String}:${rootProject.properties["parchment_mappings"] as String}@zip")
    })
}
