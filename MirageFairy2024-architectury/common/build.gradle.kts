architectury {
    common((rootProject.findProperty("enabled_platforms") as String).split(","))
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:${rootProject.findProperty("fabric_loader_version") as String}")

    // Architectury API. This is optional, and you can comment it out if you don't need it.
    modImplementation("dev.architectury:architectury:${rootProject.findProperty("architectury_api_version") as String}")
}
