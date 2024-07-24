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
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.item.Item
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

object DataGenerationEvents {
    val blockStateModelGenerators = InitializationEventRegistry<InitializationContext, (BlockStateModelGenerator) -> Unit>()
    val itemModelGenerators = InitializationEventRegistry<InitializationContext, (ItemModelGenerator) -> Unit>()
    val blockTagGenerators = InitializationEventRegistry<InitializationContext, ((TagKey<Block>) -> FabricTagProvider<Block>.FabricTagBuilder) -> Unit>()
    val itemTagGenerators = InitializationEventRegistry<InitializationContext, ((TagKey<Item>) -> FabricTagProvider<Item>.FabricTagBuilder) -> Unit>()
    val biomeTagGenerators = InitializationEventRegistry<InitializationContext, ((TagKey<Biome>) -> FabricTagProvider<Biome>.FabricTagBuilder) -> Unit>()
    val blockLootTableGenerators = InitializationEventRegistry<InitializationContext, (FabricBlockLootTableProvider) -> Unit>()
    val recipeGenerators = InitializationEventRegistry<InitializationContext, ((RecipeJsonProvider) -> Unit) -> Unit>()
    val englishTranslationGenerators = InitializationEventRegistry<InitializationContext, (FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val japaneseTranslationGenerators = InitializationEventRegistry<InitializationContext, (FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val ninePatchTextureGenerators = InitializationEventRegistry<InitializationContext, ((Identifier, NinePatchTextureCard) -> Unit) -> Unit>()
    val soundGenerators = InitializationEventRegistry<InitializationContext, ((path: String, subtitle: String?, sounds: List<Identifier>) -> Unit) -> Unit>()

    val onBuildRegistry = InitializationEventRegistry<InitializationContext, (RegistryBuilder) -> Unit>()
}

object MirageFairy2024DataGenerator : DataGeneratorEntrypoint {

    val dynamicGenerationRegistries = mutableSetOf<RegistryKey<out Registry<*>>>()

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        Modules.init()
        val pack = fabricDataGenerator.createPack()
        pack.addProvider { output: FabricDataOutput ->
            object : FabricModelProvider(output) {
                override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) = DataGenerationEvents.blockStateModelGenerators.fire { it(blockStateModelGenerator) }
                override fun generateItemModels(itemModelGenerator: ItemModelGenerator) = DataGenerationEvents.itemModelGenerators.fire { it(itemModelGenerator) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider.BlockTagProvider(output, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.blockTagGenerators.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider.ItemTagProvider(output, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.itemTagGenerators.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            object : FabricTagProvider<Biome>(output, RegistryKeys.BIOME, registriesFuture) {
                override fun configure(arg: RegistryWrapper.WrapperLookup) = DataGenerationEvents.biomeTagGenerators.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricBlockLootTableProvider(output) {
                override fun generate() = DataGenerationEvents.blockLootTableGenerators.fire { it(this) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricRecipeProvider(output) {
                override fun generate(exporter: Consumer<RecipeJsonProvider>) = DataGenerationEvents.recipeGenerators.fire { it { recipe -> exporter.accept(recipe) } }
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
                override fun generateTranslations(translationBuilder: TranslationBuilder) = DataGenerationEvents.englishTranslationGenerators.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : FabricLanguageProvider(output, "ja_jp") {
                override fun generateTranslations(translationBuilder: TranslationBuilder) = DataGenerationEvents.japaneseTranslationGenerators.fire { it(translationBuilder) }
            }
        }
        pack.addProvider { output: FabricDataOutput ->
            object : DataProvider {
                private val pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "nine_patch_textures")
                override fun getName() = "Nine Patch Textures"
                override fun run(writer: DataWriter): CompletableFuture<*> {
                    val futures = mutableListOf<CompletableFuture<*>>()
                    DataGenerationEvents.ninePatchTextureGenerators.fire {
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
                    DataGenerationEvents.soundGenerators.fire {
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
        DataGenerationEvents.onBuildRegistry.fire { it(registryBuilder) }
    }
}
