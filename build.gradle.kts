import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "2.0.0"
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
    maven("https://maven.parchmentmc.org") // mapping
}

dependencies {
    // バージョンを変更するには、gradle.properties ファイルを参照してください。
    "minecraft"("com.mojang:minecraft:${project.properties["minecraft_version"] as String}")
    "mappings"(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${project.properties["minecraft_version"] as String}:${project.properties["parchment_mappings"] as String}@zip")
    })
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
            file.writeBytes(uri("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/main/src/main/java/$fileName").toURL().readBytes())
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
    archivesName = project.properties["archives_name"] as String
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

tasks.register("buildPages") {
    dependsOn("runDatagen")
    doLast {
        println("Building pages...")
        val en = GsonBuilder().create().fromJson(File("fabric/src/main/generated/assets/miragefairy2024/lang/en_us.json").readText(), JsonElement::class.java).asJsonObject
        val ja = GsonBuilder().create().fromJson(File("fabric/src/main/generated/assets/miragefairy2024/lang/ja_jp.json").readText(), JsonElement::class.java).asJsonObject
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
