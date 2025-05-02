import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    splitEnvironmentSourceSets()
}

sourceSets {
    named("client") {
        java {
            srcDir(rootProject.file("common/src/client/java"))
        }
        kotlin {
            srcDir(rootProject.file("common/src/client/kotlin"))
        }
    }
    main {
        java {
            srcDir(rootProject.file("common/src/main/java"))
        }
        kotlin {
            srcDir(rootProject.file("common/src/main/kotlin"))
        }
        resources {
            srcDir(rootProject.file("fabric/src/main/generated"))
        }
    }
}

configurations {
    val common by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentFabric").extendsFrom(common)

    // Files in this configuration will be bundled into your mod using the Shadow plugin.
    // Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
    // ここからアーティファクトを取得するリポジトリを追加します。
    // Loom は Minecraft とライブラリを自動的にダウンロードするために必須の Maven リポジトリを追加するため、
    // 他の MOD に依存する場合にのみこれを使用してください。
    // リポジトリの詳細については、↓を参照してください。
    // https://docs.gradle.org/current/userguide/declaring_repositories.html

    maven("https://maven.shedaniel.me") // RoughlyEnoughItems

    maven("https://maven.blamejared.com") // FauxCustomEntityData-fabric-1.20.2 //// 不安定なので lib/maven に格納

    maven("https://maven.wispforest.io") // owo-lib

    maven("https://maven.minecraftforge.net/") // com.github.glitchfiend:TerraBlender-fabric
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"] as String}")

    // ファブリック API。 これは技術的にはオプションですが、おそらくそれでも必要になるでしょう。
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"] as String}")
    "modImplementation"("net.fabricmc:fabric-language-kotlin:${rootProject.properties["fabric_kotlin_version"] as String}")
    // 次の行のコメントを解除して、非推奨のファブリック API モジュールを有効にします。
    // これらは Fabric API の製品版ディストリビューションに含まれており、後で都合の良いときに MOD を最新のモジュールに更新できるようになります。

    // modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:${rootProject.properties["fabric_api_version"] as String}")

    // Architectury API. This is optional, and you can comment it out if you don't need it.
    modImplementation("dev.architectury:architectury-fabric:${rootProject.properties["architectury_api_version"] as String}")

    //"common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(path = ":common", configuration = "transformProductionFabric"))
    implementation(project(path = ":mirrg.kotlin"))
    "shadowBundle"(project(path = ":mirrg.kotlin")) { isTransitive = false }

    "modRuntimeOnly"("me.shedaniel:RoughlyEnoughItems-fabric:16.0.799")
    "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-api-fabric:16.0.799")
    "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:16.0.799")
    "modCompileOnly"("me.shedaniel.cloth:basic-math:0.6.1")

    "modImplementation"("com.faux.fauxcustomentitydata:FauxCustomEntityData-fabric-1.21.1:13.0.1")

    "modImplementation"("io.wispforest:owo-lib:0.12.15.4+1.21")
    include("io.wispforest:owo-sentinel:0.12.15.4+1.21")

    "modApi"("me.shedaniel.cloth:cloth-config:15.0.140")

    "modImplementation"("com.github.glitchfiend:TerraBlender-fabric:1.21.1-4.1.0.8")
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
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
