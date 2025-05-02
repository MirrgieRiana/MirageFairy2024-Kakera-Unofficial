pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "MF24KU"

include("mirrg.kotlin")

include("common")
include("fabric")
include("neoforge")
