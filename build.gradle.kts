plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "2.0.0"
    id("com.modrinth.minotaur") version "2.+"
}

java {
    // Loomは自動的にsourcesJarをRemapSourcesJarタスクおよび "build" タスク(存在する場合)に添付します。
    // この行を削除すると、ソースが生成されません。
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// 生成されたリソースをメイン ソース セットに追加します。
sourceSets {
    main {
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
}

dependencies {
    // バージョンを変更するには、gradle.properties ファイルを参照してください。
    "minecraft"("com.mojang:minecraft:${project.properties["minecraft_version"] as String}")
    "mappings"("net.fabricmc:yarn:${project.properties["yarn_mappings"] as String}:v2")
    "modImplementation"("net.fabricmc:fabric-loader:${project.properties["loader_version"] as String}")

    // ファブリック API。 これは技術的にはオプションですが、おそらくそれでも必要になるでしょう。
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"] as String}")
    "modImplementation"("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_kotlin_version"] as String}")
    // 次の行のコメントを解除して、非推奨のファブリック API モジュールを有効にします。
    // これらは Fabric API の製品版ディストリビューションに含まれており、後で都合の良いときに MOD を最新のモジュールに更新できるようになります。

    // modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:${project.fabric_version}")

    "modRuntimeOnly"("me.shedaniel:RoughlyEnoughItems-fabric:12.1.725")
    "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-api-fabric:12.1.725")
    "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:12.1.725")
    "modCompileOnly"("me.shedaniel.cloth:basic-math:0.6.1")

    "modImplementation"("com.faux.fauxcustomentitydata:FauxCustomEntityData-fabric-1.20.1:6.0.1")

    "modImplementation"("io.wispforest:owo-lib:0.11.2+1.20")
    include("io.wispforest:owo-sentinel:0.11.2+1.20")

    "modApi"("me.shedaniel.cloth:cloth-config:11.1.118")

    "modImplementation"("com.github.glitchfiend:TerraBlender-fabric:1.20.1-3.0.1.7")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

tasks.named<Copy>("processResources") {
    inputs.property("version", project.version)
    exclude("**/*.pdn")
    exclude("**/*.scr.png")

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
