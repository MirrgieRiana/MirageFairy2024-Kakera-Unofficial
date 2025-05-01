pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.architectury.dev/")
        maven(url = "https://files.minecraftforge.net/maven/")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "miragefairy2024"

include("common")
include("fabric")
include("neoforge")
