architectury {
    common((rootProject.properties["enabled_platforms"] as String).split(","))
}

loom {
    splitEnvironmentSourceSets()
}

sourceSets {
    main {
        resources {
            srcDir(file("src/generated/resources"))
        }
    }
}

repositories {
    maven("https://maven.shedaniel.me") // RoughlyEnoughItems
    maven("https://maven.blamejared.com") // FauxCustomEntityData-fabric-1.20.2 //// 不安定なので lib/maven に格納
    maven("https://maven.wispforest.io/releases/") // owo-lib
    maven("https://maven.minecraftforge.net/") // com.github.glitchfiend:TerraBlender-fabric
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"] as String}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"] as String}")

    // Architectury API. This is optional, and you can comment it out if you don't need it.
    modImplementation("dev.architectury:architectury:${rootProject.properties["architectury_api_version"] as String}")

    implementation(project(path = ":mirrg.kotlin"))

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:16.0.799")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:16.0.799")
    modCompileOnly("me.shedaniel.cloth:basic-math:0.6.1")

    modImplementation("com.faux.fauxcustomentitydata:FauxCustomEntityData-fabric-1.21.1:13.0.1")

    modImplementation("io.wispforest:owo-lib:0.12.15.4+1.21")
    include("io.wispforest:owo-sentinel:0.12.15.4+1.21")

    modApi("me.shedaniel.cloth:cloth-config:15.0.140")

    modImplementation("com.github.glitchfiend:TerraBlender-fabric:1.21.1-4.1.0.8")

}
