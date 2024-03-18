package miragefairy2024

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
import net.minecraft.block.Block
import net.minecraft.data.DataOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.DataWriter
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.Item
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

object MirageFairy2024DataGenerator : DataGeneratorEntrypoint {

    val blockStateModelGenerators = InitializationEventRegistry<(BlockStateModelGenerator) -> Unit>()
    val itemModelGenerators = InitializationEventRegistry<(ItemModelGenerator) -> Unit>()
    val blockTagGenerators = InitializationEventRegistry<((TagKey<Block>) -> FabricTagProvider<Block>.FabricTagBuilder) -> Unit>()
    val itemTagGenerators = InitializationEventRegistry<((TagKey<Item>) -> FabricTagProvider<Item>.FabricTagBuilder) -> Unit>()
    val blockLootTableGenerators = InitializationEventRegistry<(FabricBlockLootTableProvider) -> Unit>()
    val recipeGenerators = InitializationEventRegistry<(RecipeExporter) -> Unit>()
    val dynamicGenerationRegistries = mutableSetOf<RegistryKey<out Registry<*>>>()
    val englishTranslationGenerators = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val japaneseTranslationGenerators = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val ninePatchTextureGenerators = InitializationEventRegistry<((Identifier, NinePatchTextureCard) -> Unit) -> Unit>()
    val soundGenerators = InitializationEventRegistry<((path: String, subtitle: String?, sounds: List<Identifier>) -> Unit) -> Unit>()

    val onBuildRegistry = InitializationEventRegistry<(RegistryBuilder) -> Unit>()

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider { output: FabricDataOutput ->
            object : FabricModelProvider(output) {
                override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) = blockStateModelGenerators.fire { it(blockStateModelGenerator) }
                override fun generateItemModels(itemModelGenerator: ItemModelGenerator) = itemModelGenerators.fire { it(itemModelGenerator) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider.BlockTagProvider(output, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = blockTagGenerators.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider.ItemTagProvider(output, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = itemTagGenerators.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricBlockLootTableProvider(output) {
                override fun generate() = blockLootTableGenerators.fire { it(this) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricRecipeProvider(output) {
                override fun generate(exporter: RecipeExporter) = recipeGenerators.fire { it(exporter) }
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
                override fun generateTranslations(translationBuilder: TranslationBuilder) = englishTranslationGenerators.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricLanguageProvider(output, "ja_jp") {
                override fun generateTranslations(translationBuilder: TranslationBuilder) = japaneseTranslationGenerators.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "nine_patch_textures")
                override fun getName() = "Nine Patch Textures"
                override fun run(writer: DataWriter): CompletableFuture<*> {
                    val futures = mutableListOf<CompletableFuture<*>>()
                    ninePatchTextureGenerators.fire {
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
                private val destination = Identifier(MirageFairy2024.modId, "sounds")
                override fun getName() = "Sounds"
                override fun run(writer: DataWriter): CompletableFuture<*> {

                    val map = mutableMapOf<String, Pair<String?, List<Identifier>>>()
                    soundGenerators.fire {
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
    }

    override fun buildRegistry(registryBuilder: RegistryBuilder) {
        onBuildRegistry.fire { it(registryBuilder) }
    }
}
