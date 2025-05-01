/*/*/*/*/*
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.net.URL

plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "2.1.20"
    id("com.modrinth.minotaur") version "2.+"
}

java {
    // Loomは自動的にsourcesJarをRemapSourcesJarタスクおよび "build" タスク(存在する場合)に添付します。
    // この行を削除すると、ソースが生成されません。
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// 生成されたリソースをメイン ソース セットに追加します。
sourceSets {
    main {
        java {
            srcDir("lib/mirrg.kotlin")
        }
        resources {
            srcDir(
                "src/main/generated"
            )
        }
    }
}

// configurationの追加のためにdependenciesより上にある必要がある
loom {
    splitEnvironmentSourceSets()

    mods {
        register("miragefairy2024") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
    runs {
        // これにより、datagen API を実行する新しい gradle タスク "gradlew runDatagen" が追加されます。
        register("datagen") {
            inherit(runs["server"])
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated")}")
            vmArg("-Dfabric-api.datagen.modid=miragefairy2024")

            runDir("build/datagen")
        }
        named("client") {
            vmArgs += listOf("-Xmx4G")
            programArgs += listOf("--username", "Player1")
        }
        named("server") {
            runDir = "run_server" // ファイルロックを回避しクライアントと同時に起動可能にする
        }
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

    maven("https://maven.parchmentmc.org") // mapping
}

dependencies {
    // バージョンを変更するには、gradle.properties ファイルを参照してください。
    "minecraft"("com.mojang:minecraft:${project.properties["minecraft_version"] as String}")
    "mappings"(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${project.properties["minecraft_version"] as String}:${project.properties["parchment_mappings"] as String}@zip")
    })
    "modImplementation"("net.fabricmc:fabric-loader:${project.properties["loader_version"] as String}")

    // ファブリック API。 これは技術的にはオプションですが、おそらくそれでも必要になるでしょう。
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"] as String}")
    "modImplementation"("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"] as String}")
    // 次の行のコメントを解除して、非推奨のファブリック API モジュールを有効にします。
    // これらは Fabric API の製品版ディストリビューションに含まれており、後で都合の良いときに MOD を最新のモジュールに更新できるようになります。

    // modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:${project.fabric_version}")

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

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

tasks.register("fetchMirrgKotlin") {
    doFirst {
        fun fetch(fileName: String) {
            val file = project.rootDir.resolve("lib/mirrg.kotlin").resolve(fileName)
            when {
                file.parentFile.isDirectory -> Unit
                file.parentFile.exists() -> throw RuntimeException("Already exists: ${file.parentFile}")
                !file.parentFile.mkdirs() -> throw RuntimeException("Could not create the directory: ${file.parentFile}")
            }
            file.writeBytes(URL("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/main/src/main/java/$fileName").readBytes())
        }
        fetch("mirrg/kotlin/gson/hydrogen/Gson.kt")
        fetch("mirrg/kotlin/gson/hydrogen/JsonWrapper.kt")
        fetch("mirrg/kotlin/hydrogen/Lang.kt")
        fetch("mirrg/kotlin/hydrogen/Number.kt")
        fetch("mirrg/kotlin/hydrogen/String.kt")
        fetch("mirrg/kotlin/java/hydrogen/Number.kt")
        fetch("mirrg/kotlin/slf4j/hydrogen/Logging.kt")
    }
}

tasks.named<Copy>("processResources") {
    inputs.property("version", project.version)
    exclude("**/*.pdn")
    exclude("**/*.scr.png")
    exclude("**/*.sc2.png")
    exclude("**/*.wav")

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.named<Jar>("jar") {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

version = project.properties["mod_version"] as String
group = project.properties["maven_group"] as String

base {
    archivesName = project.properties["archives_base_name"] as String
}

tasks["modrinth"].dependsOn(tasks["modrinthSyncBody"])

// https://github.com/modrinth/minotaur
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "miragefairy2024-kakera-unofficial"
    //versionNumber = project.mod_version
    versionType = "beta"
    uploadFile = tasks["remapJar"]
    //gameVersions = ["1.20.2"]
    //loaders = ["fabric"]
    syncBodyFrom = rootProject.file("MODRINTH-BODY.md").readText()
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
        required.project("faux-custom-entity-data")
        required.project("owo-lib")
        required.project("cloth-config")
        required.project("terrablender")
    }
}

// Mavenパブリケーションの構成
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // 公開の設定方法については、https://docs.gradle.org/current/userguide/publishing_maven.html を参照してください。
    repositories {
        // ここに公開するリポジトリを追加します。
        // 注意: このブロックには、最上位のブロックと同じ機能はありません。
        // ここのリポジトリは、依存関係を取得するためではなく、アーティファクトを公開するために使用されます。
    }
}

tasks.register("buildPages") {
    dependsOn("runDatagen")
    doLast {
        println("Building pages...")
        val en = GsonBuilder().create().fromJson(File("src/main/generated/assets/miragefairy2024/lang/en_us.json").readText(), JsonElement::class.java).asJsonObject
        val ja = GsonBuilder().create().fromJson(File("src/main/generated/assets/miragefairy2024/lang/ja_jp.json").readText(), JsonElement::class.java).asJsonObject
        val keys = (en.keySet() + en.keySet()).sorted()

        mkdir("build/pages")
        """
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>MF24KU Lang Table</title>
    <style>
        #langTable {
            border-collapse: collapse;
        }

        #langTable th {
            background-color: #ddd;
        }
        #langTable tr:nth-child(even) {
            background-color: #eee;
        }

        #langTable th, #langTable td {
            padding: 0 0.5em;
        }
        #langTable th.value, #langTable td.value {
            border-left: 1px solid #888;
        }

        #langTable td.key {
            word-break: break-all;
        }
        #langTable td.value {
            white-space: pre-line;
            vertical-align: top;
        }
    </style>
</head>
<body>
<h1>MF24KU Lang Table</h1>
<table id="langTable">
    <colgroup>
        <col style="width: 20%;">
        <col style="width: 40%;">
        <col style="width: 40%;">
    </colgroup>
    <thead>
    <tr>
        <th class="key">Key</th>
        <th class="value">English</th>
        <th class="value">Japanese</th>
    </tr>
    </thead>
    <tbody>
        ${
            keys.joinToString("") { key ->
                """
<tr>
    <td class="key">$key</td>
    <td class="value">${(en.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>
    <td class="value">${(ja.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>
</tr>
                """.trimIndent()
            }
        }
    </tbody>
</table>
</body>
</html>
        """.let { File("build/pages/lang_table.html").writeText(it) }
        """
[MF24KU Lang Table](lang_table.html)
        """.let { File("build/pages/index.md").writeText(it) }
    }
}
*/

plugins {
    id 'dev.architectury.loom' version '1.7-SNAPSHOT' apply false
    id 'architectury-plugin' version '3.4-SNAPSHOT'
    id 'com.github.johnrengelman.shadow' version '8.1.1' apply false
}

architectury {
    minecraft = project.minecraft_version
}

allprojects {
    group = rootProject.maven_group
    version = rootProject.mod_version
}

subprojects {
    apply plugin: 'dev.architectury.loom'
    apply plugin: 'architectury-plugin'
    apply plugin: 'maven-publish'

    base {
        // Set up a suffixed format for the mod jar names, e.g. `example-fabric`.
        archivesName = "$rootProject.archives_name-$project.name"
    }

    repositories {
        // Add repositories to retrieve artifacts from in here.
        // You should only use this when depending on other mods because
        // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
        // See https://docs.gradle.org/current/userguide/declaring_repositories.html
        // for more information about repositories.
    }

    loom {
        silentMojangMappingsLicense()
    }

    dependencies {
        minecraft "net.minecraft:minecraft:$rootProject.minecraft_version"
        mappings loom.officialMojangMappings()
    }

    java {
        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
        // if it is present.
        // If you remove this line, sources will not be generated.
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType(JavaCompile).configureEach {
        it.options.release = 21
    }

    // Configure Maven publishing.
    publishing {
        publications {
            mavenJava(MavenPublication) {
                artifactId = base.archivesName.get()
                from components.java
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
