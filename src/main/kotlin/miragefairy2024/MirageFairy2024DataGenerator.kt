package miragefairy2024

import com.google.gson.JsonElement
import miragefairy2024.mod.NinePatchTextureCard
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
import net.minecraft.block.Block
import net.minecraft.data.DataOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.DataWriter
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.entity.EntityType
import net.minecraft.entity.damage.DamageType
import net.minecraft.item.Item
import net.minecraft.loot.LootTable
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
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
    val onGenerateEntityTypeTag = InitializationEventRegistry<((TagKey<EntityType<*>>) -> FabricTagProvider<EntityType<*>>.FabricTagBuilder) -> Unit>()
    val onGenerateDamageTypeTag = InitializationEventRegistry<((TagKey<DamageType>) -> FabricTagProvider<DamageType>.FabricTagBuilder) -> Unit>()
    val onGenerateBlockLootTable = InitializationEventRegistry<(FabricBlockLootTableProvider) -> Unit>()
    val onGenerateChestLootTable = InitializationEventRegistry<((Identifier, LootTable.Builder) -> Unit) -> Unit>()
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
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider.BlockTagProvider(output, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.onGenerateBlockTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider.ItemTagProvider(output, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.onGenerateItemTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider<Biome>(output, RegistryKeys.BIOME, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.onGenerateBiomeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider<EntityType<*>>(output, RegistryKeys.ENTITY_TYPE, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.onGenerateEntityTypeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider<DamageType>(output, RegistryKeys.DAMAGE_TYPE, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.onGenerateDamageTypeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricBlockLootTableProvider(output) {
                override fun generate() = DataGenerationEvents.onGenerateBlockLootTable.fire { it(this) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : SimpleFabricLootTableProvider(output, LootContextTypes.CHEST) {
                override fun accept(exporter: BiConsumer<Identifier, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateChestLootTable.fire { it { lootTableId, builder -> exporter.accept("chests/" * lootTableId, builder) } }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : SimpleFabricLootTableProvider(output, LootContextTypes.ENTITY) {
                override fun accept(exporter: BiConsumer<Identifier, LootTable.Builder>) {
                    DataGenerationEvents.onGenerateEntityLootTable.fire { it { entityType, builder -> exporter.accept(entityType.lootTableId, builder) } }
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricRecipeProvider(output) {
                override fun generate(exporter: Consumer<RecipeJsonProvider>) = DataGenerationEvents.onGenerateRecipe.fire { it { recipe -> exporter.accept(recipe) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricDynamicRegistryProvider(output, registriesFuture) {
                override fun getName() = "World Gen"
                override fun configure(registries: RegistryWrapper.WrapperLookup, entries: Entries) {
                    dynamicGenerationRegistries.forEach {
                        entries.addAll(registries.getWrapperOrThrow(it))
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
                private val pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "nine_patch_textures")
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
                            futures.add(DataProvider.writeToPath(writer, data, pathResolver.resolveJson(identifier)))
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

                    val path = output.resolvePath(DataOutput.OutputType.RESOURCE_PACK).resolve(destination.namespace).resolve(destination.path + ".json")

                    val jsonElement = map.map { (path, entry) ->
                        path to jsonObjectNotNull(
                            entry.first?.let { "subtitle" to it.jsonElement },
                            "sounds" to entry.second.map { it.string.jsonElement }.jsonArray,
                        )
                    }.jsonObject

                    return DataProvider.writeToPath(writer, jsonElement, path)
                }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "particles")
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
                        futures.add(DataProvider.writeToPath(writer, jsonElement, pathResolver.resolveJson(identifier)))
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
