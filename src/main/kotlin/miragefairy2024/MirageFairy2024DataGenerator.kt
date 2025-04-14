package miragefairy2024

import com.google.gson.JsonElement
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.util.string
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
import net.minecraft.world.level.block.Block
import net.minecraft.data.PackOutput as DataOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.CachedOutput as DataWriter
import net.minecraft.data.models.BlockModelGenerators as BlockStateModelGenerator
import net.minecraft.data.models.ItemModelGenerators as ItemModelGenerator
import net.minecraft.data.recipes.FinishedRecipe as RecipeJsonProvider
import net.minecraft.world.entity.EntityType
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.item.Item
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets as LootContextTypes
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder as RegistryBuilder
import net.minecraft.resources.ResourceKey as RegistryKey
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.core.HolderLookup as RegistryWrapper
import net.minecraft.tags.TagKey
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Consumer

object DataGenerationEvents {
    val onInitializeDataGenerator = InitializationEventRegistry<() -> Unit>()

    val onGenerateBlockStateModel = InitializationEventRegistry<(BlockStateModelGenerator) -> Unit>()
    val onGenerateItemModel = InitializationEventRegistry<(ItemModelGenerator) -> Unit>()
    val onGenerateBlockTag = InitializationEventRegistry<((TagKey<Block>) -> FabricTagProvider<Block>.FabricTagBuilder) -> Unit>()
    val onGenerateItemTag = InitializationEventRegistry<((TagKey<Item>) -> FabricTagProvider<Item>.FabricTagBuilder) -> Unit>()
    val onGenerateBiomeTag = InitializationEventRegistry<((TagKey<Biome>) -> FabricTagProvider<Biome>.FabricTagBuilder) -> Unit>()
    val onGenerateStructureTag = InitializationEventRegistry<((TagKey<Structure>) -> FabricTagProvider<Structure>.FabricTagBuilder) -> Unit>()
    val onGenerateEntityTypeTag = InitializationEventRegistry<((TagKey<EntityType<*>>) -> FabricTagProvider<EntityType<*>>.FabricTagBuilder) -> Unit>()
    val onGenerateDamageTypeTag = InitializationEventRegistry<((TagKey<DamageType>) -> FabricTagProvider<DamageType>.FabricTagBuilder) -> Unit>()
    val onGenerateBlockLootTable = InitializationEventRegistry<(FabricBlockLootTableProvider) -> Unit>()
    val onGenerateChestLootTable = InitializationEventRegistry<((Identifier, LootTable.Builder) -> Unit) -> Unit>()
    val onGenerateArchaeologyLootTable = InitializationEventRegistry<((Identifier, LootTable.Builder) -> Unit) -> Unit>()
    val onGenerateEntityLootTable = InitializationEventRegistry<((EntityType<*>, LootTable.Builder) -> Unit) -> Unit>()
    val onGenerateRecipe = InitializationEventRegistry<((RecipeJsonProvider) -> Unit) -> Unit>()
    val onGenerateEnglishTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateJapaneseTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateNinePatchTexture = InitializationEventRegistry<((Identifier, NinePatchTextureCard) -> Unit) -> Unit>()
    val onGenerateSound = InitializationEventRegistry<((path: String, subtitle: String?, sounds: List<Identifier>) -> Unit) -> Unit>()
    val onGenerateParticles = InitializationEventRegistry<((identifier: Identifier, jsonElement: JsonElement) -> Unit) -> Unit>()

    val onBuildRegistry = InitializationEventRegistry<(RegistryBuilder) -> Unit>()
}

object MirageFairy2024DataGenerator : DataGeneratorEntrypoint {

    val dynamicGenerationRegistries = mutableSetOf<RegistryKey<out Registry<*>>>()

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        Modules.init()
        DataGenerationEvents.onInitializeDataGenerator.fire { it() }

        val pack = fabricDataGenerator.createPack()
        pack.addProvider { output: FabricDataOutput ->
            object : FabricModelProvider(output) {
                override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) = DataGenerationEvents.onGenerateBlockStateModel.fire { it(blockStateModelGenerator) }
                override fun generateItemModels(itemModelGenerator: ItemModelGenerator) = DataGenerationEvents.onGenerateItemModel.fire { it(itemModelGenerator) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.Provider> ->
            object : FabricTagProvider.BlockTagProvider(output, registriesFuture) {
                override fun addTags(arg: RegistryWrapper.Provider) = DataGenerationEvents.onGenerateBlockTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.Provider> ->
            object : FabricTagProvider.ItemTagProvider(output, registriesFuture) {
                override fun addTags(arg: RegistryWrapper.Provider) = DataGenerationEvents.onGenerateItemTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.Provider> ->
            object : FabricTagProvider<Biome>(output, RegistryKeys.BIOME, registriesFuture) {
                override fun addTags(arg: RegistryWrapper.Provider) = DataGenerationEvents.onGenerateBiomeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.Provider> ->
            object : FabricTagProvider<Structure>(output, RegistryKeys.STRUCTURE, registriesFuture) {
                override fun addTags(arg: RegistryWrapper.Provider) = DataGenerationEvents.onGenerateStructureTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.Provider> ->
            object : FabricTagProvider<EntityType<*>>(output, RegistryKeys.ENTITY_TYPE, registriesFuture) {
                override fun addTags(arg: RegistryWrapper.Provider) = DataGenerationEvents.onGenerateEntityTypeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.Provider> ->
            object : FabricTagProvider<DamageType>(output, RegistryKeys.DAMAGE_TYPE, registriesFuture) {
                override fun addTags(arg: RegistryWrapper.Provider) = DataGenerationEvents.onGenerateDamageTypeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricBlockLootTableProvider(output) {
                override fun generate() = DataGenerationEvents.onGenerateBlockLootTable.fire { it(this) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : SimpleFabricLootTableProvider(output, LootContextTypes.CHEST) {
                override fun generate(exporter: BiConsumer<Identifier, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateChestLootTable.fire { it { lootTableId, builder -> exporter.accept(lootTableId, builder) } }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : SimpleFabricLootTableProvider(output, LootContextTypes.ARCHAEOLOGY) {
                override fun generate(exporter: BiConsumer<Identifier, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateArchaeologyLootTable.fire { it { lootTableId, builder -> exporter.accept(lootTableId, builder) } }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : SimpleFabricLootTableProvider(output, LootContextTypes.ENTITY) {
                override fun generate(exporter: BiConsumer<Identifier, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateEntityLootTable.fire { it { entityType, builder -> exporter.accept(entityType.lootTable, builder) } }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricRecipeProvider(output) {
                override fun buildRecipes(exporter: Consumer<RecipeJsonProvider>) = DataGenerationEvents.onGenerateRecipe.fire { it { recipe -> exporter.accept(recipe) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.Provider> ->
            object : FabricDynamicRegistryProvider(output, registriesFuture) {
                override fun getName() = "World Gen"
                override fun configure(registries: RegistryWrapper.Provider, entries: Entries) {
                    dynamicGenerationRegistries.forEach {
                        entries.addAll(registries.lookupOrThrow(it))
                    }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricLanguageProvider(output, "en_us") {
                override fun generateTranslations(translationBuilder: TranslationBuilder) = DataGenerationEvents.onGenerateEnglishTranslation.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricLanguageProvider(output, "ja_jp") {
                override fun generateTranslations(translationBuilder: TranslationBuilder) = DataGenerationEvents.onGenerateJapaneseTranslation.fire { it(translationBuilder) }
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

                    val map = mutableMapOf<String, Pair<String?, List<Identifier>>>()
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

                    val map = mutableMapOf<Identifier, JsonElement>()
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
    }

    override fun buildRegistry(registryBuilder: RegistryBuilder) {
        DataGenerationEvents.onBuildRegistry.fire { it(registryBuilder) }
    }
}
