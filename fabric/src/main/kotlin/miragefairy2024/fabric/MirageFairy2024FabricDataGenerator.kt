package miragefairy2024.fabric

import com.google.gson.JsonElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import miragefairy2024.DataGenerationEvents
import miragefairy2024.DataMapConsumer
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.Modules
import miragefairy2024.platformProxy
import miragefairy2024.util.string
import miragefairy2024.util.times
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.data.DataProvider
import net.minecraft.data.advancements.AdvancementProvider
import net.minecraft.data.advancements.AdvancementSubProvider
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootTable
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Consumer
import net.minecraft.core.RegistrySetBuilder as RegistryBuilder
import net.minecraft.data.CachedOutput as DataWriter
import net.minecraft.data.PackOutput as DataOutput
import net.minecraft.data.models.BlockModelGenerators as BlockStateModelGenerator
import net.minecraft.data.models.ItemModelGenerators as ItemModelGenerator
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets as LootContextTypes

object MirageFairy2024FabricDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        with(ModContext()) {
            platformProxy = FabricPlatformProxy()
            Modules.init()
            initFabricModule()
        }
        DataGenerationEvents.onInitializeDataGenerator.fire { it() }

        val pack = fabricDataGenerator.createPack()
        when (val platform = System.getProperty("miragefairy2024.datagen.platform")) {
            "common" -> common(pack)
            "neoforge" -> neoForge(pack)
            else -> throw IllegalArgumentException("Unknown platform: $platform")
        }
    }

    private fun common(pack: FabricDataGenerator.Pack) {
        pack.addProvider { output: FabricDataOutput ->
            object : FabricModelProvider(output) {
                override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) = DataGenerationEvents.onGenerateBlockModel.fire { it(blockStateModelGenerator) }
                override fun generateItemModels(itemModelGenerator: ItemModelGenerator) = DataGenerationEvents.onGenerateItemModel.fire { it(itemModelGenerator) }
            }
        }
        run {
            pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
                val eventRegistry = DataGenerationEvents.onGenerateBlockTag
                val adder: ((TagKey<Block>) -> FabricTagProvider<Block>.FabricTagBuilder) -> Unit = { builderFactory ->
                    eventRegistry.fire { listener -> listener(builderFactory) }
                }
                object : FabricTagProvider.BlockTagProvider(output, registriesFuture) {
                    override fun addTags(arg: HolderLookup.Provider) = adder { tag -> getOrCreateTagBuilder(tag) }
                }
            }
        }
        run {
            pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
                val eventRegistry = DataGenerationEvents.onGenerateItemTag
                val adder: ((TagKey<Item>) -> FabricTagProvider<Item>.FabricTagBuilder) -> Unit = { builderFactory ->
                    eventRegistry.fire { listener -> listener(builderFactory) }
                }
                object : FabricTagProvider.ItemTagProvider(output, registriesFuture) {
                    override fun addTags(arg: HolderLookup.Provider) = adder { tag -> getOrCreateTagBuilder(tag) }
                }
            }
        }
        fun <T> f(registryKey: ResourceKey<out Registry<T>>, eventRegistry: InitializationEventRegistry<((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit>) {
            pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
                val adder: ((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit = { builderFactory ->
                    eventRegistry.fire { listener -> listener(builderFactory) }
                }
                object : FabricTagProvider<T>(output, registryKey, registriesFuture) {
                    override fun addTags(arg: HolderLookup.Provider) = adder { tag -> getOrCreateTagBuilder(tag) }
                }
            }
        }
        f(Registries.BIOME, DataGenerationEvents.onGenerateBiomeTag)
        f(Registries.STRUCTURE, DataGenerationEvents.onGenerateStructureTag)
        f(Registries.ENTITY_TYPE, DataGenerationEvents.onGenerateEntityTypeTag)
        f(Registries.DAMAGE_TYPE, DataGenerationEvents.onGenerateDamageTypeTag)
        f(Registries.ENCHANTMENT, DataGenerationEvents.onGenerateEnchantmentTag)
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : FabricBlockLootTableProvider(output, registriesFuture) {
                override fun generate() {
                    DataGenerationEvents.onGenerateBlockLootTable.fire { it(this, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextTypes.CHEST) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateChestLootTable.fire { it({ lootTableId, builder -> exporter.accept(lootTableId, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextTypes.ARCHAEOLOGY) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateArchaeologyLootTable.fire { it({ lootTableId, builder -> exporter.accept(lootTableId, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextTypes.ENTITY) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateEntityLootTable.fire { it({ entityType, builder -> exporter.accept(entityType.defaultLootTable, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            val registries = registriesFuture.join()
            object : SimpleFabricLootTableProvider(output, registriesFuture, LootContextTypes.ADVANCEMENT_REWARD) {
                override fun generate(exporter: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateAdvancementRewardLootTable.fire { it({ lootTableId, builder -> exporter.accept(lootTableId, builder) }, registries) }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricRecipeProvider(output, registriesFuture) {
                override fun buildRecipes(recipeOutput: RecipeOutput) = DataGenerationEvents.onGenerateRecipe.fire { it(recipeOutput) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricDynamicRegistryProvider(output, registriesFuture) {
                override fun getName() = "World Gen"
                override fun configure(registries: HolderLookup.Provider, entries: Entries) {
                    DataGenerationEvents.dynamicGenerationRegistries.forEach {
                        entries.addAll(registries.lookupOrThrow(it))
                    }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricLanguageProvider(output, "en_us", registriesFuture) {
                override fun generateTranslations(holderLookupProvider: HolderLookup.Provider, translationBuilder: TranslationBuilder) = DataGenerationEvents.onGenerateEnglishTranslation.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricLanguageProvider(output, "ja_jp", registriesFuture) {
                override fun generateTranslations(holderLookupProvider: HolderLookup.Provider, translationBuilder: TranslationBuilder) = DataGenerationEvents.onGenerateJapaneseTranslation.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val pathResolver = output.createPathProvider(DataOutput.Target.RESOURCE_PACK, "nine_patch_textures")
                override fun getName() = "Nine Patch Textures"
                override fun run(writer: DataWriter): CompletableFuture<*> {
                    val futures = mutableListOf<CompletableFuture<*>>()
                    DataGenerationEvents.onGenerateNinePatchTexture.fire {
                        it { identifier, card ->
                            val data = jsonObject(
                                "texture" to card.texture.string.jsonElement,
                                "texture_width" to card.textureWidth.jsonElement,
                                "texture_height" to card.textureHeight.jsonElement,
                                "repeat" to card.repeat.jsonElement,
                                "patch_size" to jsonObject(
                                    "width" to card.patchWidth.jsonElement,
                                    "height" to card.patchHeight.jsonElement,
                                ),
                            )
                            futures.add(DataProvider.saveStable(writer, data, pathResolver.json(identifier)))
                        }
                    }
                    return CompletableFuture.allOf(*futures.toTypedArray())
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val destination = MirageFairy2024.identifier("sounds")
                override fun getName() = "Sounds"
                override fun run(writer: DataWriter): CompletableFuture<*> {

                    val map = mutableMapOf<String, Pair<String?, List<ResourceLocation>>>()
                    DataGenerationEvents.onGenerateSound.fire {
                        it { path, subtitle, sounds ->
                            map[path] = Pair(subtitle, sounds)
                        }
                    }
                    if (map.isEmpty()) return CompletableFuture.allOf()

                    val path = output.getOutputFolder(DataOutput.Target.RESOURCE_PACK).resolve(destination.namespace).resolve(destination.path + ".json")

                    val jsonElement = map.map { (path, entry) ->
                        path to jsonObjectNotNull(
                            entry.first?.let { "subtitle" to it.jsonElement },
                            "sounds" to entry.second.map { it.string.jsonElement }.jsonArray,
                        )
                    }.jsonObject

                    return DataProvider.saveStable(writer, jsonElement, path)
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val pathResolver = output.createPathProvider(DataOutput.Target.RESOURCE_PACK, "particles")
                override fun getName() = "Particles"
                override fun run(writer: DataWriter): CompletableFuture<*> {

                    val map = mutableMapOf<ResourceLocation, JsonElement>()
                    DataGenerationEvents.onGenerateParticles.fire {
                        it { identifier, jsonElement ->
                            if (identifier in map) throw Exception("Duplicate particle definition for $identifier")
                            map[identifier] = jsonElement
                        }
                    }

                    val futures = mutableListOf<CompletableFuture<*>>()
                    map.forEach { (identifier, jsonElement) ->
                        futures.add(DataProvider.saveStable(writer, jsonElement, pathResolver.json(identifier)))
                    }
                    return CompletableFuture.allOf(*futures.toTypedArray())
                }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            AdvancementProvider(output, registriesFuture, listOf(object : AdvancementSubProvider {
                override fun generate(registries: HolderLookup.Provider, writer: Consumer<AdvancementHolder>) {
                    runBlocking {
                        DataGenerationEvents.onGenerateAdvancement.fire {
                            launch {
                                it(registries, writer)
                            }
                        }
                    }
                }
            }))
        }
    }

    private fun neoForge(pack: FabricDataGenerator.Pack) {
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : DataProvider {
                private val pathResolver = output.createPathProvider(DataOutput.Target.DATA_PACK, "data_maps")
                override fun getName() = "Data Maps"
                override fun run(writer: DataWriter): CompletableFuture<*> {
                    val registries = registriesFuture.join()
                    val registryMap = mutableMapOf<ResourceKey<out Registry<*>>, MutableMap<ResourceLocation, MutableMap<ResourceLocation, JsonElement>>>()
                    DataGenerationEvents.onGenerateDataMap.fire {
                        it(object : DataMapConsumer {
                            override fun <T> accept(registry: ResourceKey<Registry<T>>, type: ResourceLocation, target: ResourceKey<T>, data: JsonElement) {
                                val typeMap = registryMap.getOrPut(registry) { mutableMapOf() }
                                val targetMap = typeMap.getOrPut(type) { mutableMapOf() }
                                if (target.location() in targetMap) throw IllegalArgumentException("Duplicate data map entry for ${target.location()} in registry ${registry.location()}")
                                targetMap[target.location()] = data
                            }
                        }, registries)
                    }
                    val futures = mutableListOf<CompletableFuture<*>>()
                    registryMap.forEach { (registry, typeMap) ->
                        typeMap.forEach { (type, targetMap) ->
                            val jsonElement = jsonObject(
                                "values" to targetMap.mapKeys { it.key.string }.jsonObject,
                            )
                            val registryPath = (if (registry.location().namespace == "minecraft") "" else "${registry.location().namespace}/") + registry.location().path
                            futures += DataProvider.saveStable(writer, jsonElement, pathResolver.json("$registryPath/" * type))
                        }
                    }
                    return CompletableFuture.allOf(*futures.toTypedArray())
                }
            }
        }
    }

    override fun buildRegistry(registryBuilder: RegistryBuilder) {
        DataGenerationEvents.onBuildRegistry.fire { it(registryBuilder) }
    }
}
