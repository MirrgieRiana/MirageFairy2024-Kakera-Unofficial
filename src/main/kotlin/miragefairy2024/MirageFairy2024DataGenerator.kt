package miragefairy2024

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
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.Item
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.TagKey
import java.util.concurrent.CompletableFuture

object MirageFairy2024DataGenerator : DataGeneratorEntrypoint {

    val blockStateModelGenerators = DataGeneratorRegistry<(BlockStateModelGenerator) -> Unit>()
    val itemModelGenerators = DataGeneratorRegistry<(ItemModelGenerator) -> Unit>()
    val blockTagGenerators = DataGeneratorRegistry<((TagKey<Block>) -> FabricTagProvider<Block>.FabricTagBuilder) -> Unit>()
    val itemTagGenerators = DataGeneratorRegistry<((TagKey<Item>) -> FabricTagProvider<Item>.FabricTagBuilder) -> Unit>()
    val blockLootTableGenerators = DataGeneratorRegistry<(FabricBlockLootTableProvider) -> Unit>()
    val recipeGenerators = DataGeneratorRegistry<(RecipeExporter) -> Unit>()
    val dynamicGenerationRegistries = mutableSetOf<RegistryKey<out Registry<*>>>()
    val englishTranslationGenerators = DataGeneratorRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val japaneseTranslationGenerators = DataGeneratorRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()

    val onBuildRegistry = mutableListOf<(RegistryBuilder) -> Unit>()

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
    }

    override fun buildRegistry(registryBuilder: RegistryBuilder) {
        onBuildRegistry.forEach {
            it(registryBuilder)
        }
    }
}

class DataGeneratorRegistry<T> {
    val list = mutableListOf<T>()
    var closed = false

    operator fun invoke(listener: T) {
        require(!closed)
        this.list += listener
    }

    fun fire(processor: (T) -> Unit) {
        closed = true
        this.list.forEach {
            processor(it)
        }
    }
}
