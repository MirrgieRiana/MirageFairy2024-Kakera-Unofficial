package miragefairy2024.fabric

import com.google.gson.JsonElement
import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.Modules
import miragefairy2024.mod.MaterialCard
import miragefairy2024.platformProxy
import miragefairy2024.util.invoke
import miragefairy2024.util.string
import miragefairy2024.util.text
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
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.DataProvider
import net.minecraft.data.advancements.AdvancementProvider
import net.minecraft.data.advancements.AdvancementSubProvider
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure
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
        pack.addProvider { output: FabricDataOutput ->
            object : FabricModelProvider(output) {
                override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) = DataGenerationEvents.onGenerateBlockStateModel.fire { it(blockStateModelGenerator) }
                override fun generateItemModels(itemModelGenerator: ItemModelGenerator) = DataGenerationEvents.onGenerateItemModel.fire { it(itemModelGenerator) }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricTagProvider.BlockTagProvider(output, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = DataGenerationEvents.onGenerateBlockTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricTagProvider.ItemTagProvider(output, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = DataGenerationEvents.onGenerateItemTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricTagProvider<Biome>(output, Registries.BIOME, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = DataGenerationEvents.onGenerateBiomeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricTagProvider<Structure>(output, Registries.STRUCTURE, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = DataGenerationEvents.onGenerateStructureTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricTagProvider<EntityType<*>>(output, Registries.ENTITY_TYPE, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = DataGenerationEvents.onGenerateEntityTypeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricTagProvider<DamageType>(output, Registries.DAMAGE_TYPE, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = DataGenerationEvents.onGenerateDamageTypeTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider> ->
            object : FabricTagProvider<Enchantment>(output, Registries.ENCHANTMENT, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = DataGenerationEvents.onGenerateEnchantmentTag.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
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

                    val advancementHolder = Advancement.Builder.advancement()
                        .display(
                            MaterialCard.MIRAGE_FLOUR.item,
                            text { "植物の支配する世界"() },
                            text { "妖花ミラージュは右クリックで収穫できる"() },
                            MirageFairy2024.identifier("textures/block/aura_stone.png"),
                            AdvancementType.TASK,
                            false,
                            false,
                            false
                        )
                        .addCriterion("has_mirage_flour", InventoryChangeTrigger.TriggerInstance.hasItems(MaterialCard.MIRAGE_FLOUR.item))
                        .save(writer, MirageFairy2024.identifier("main/a1").string)
                    val advancementHolder2 = Advancement.Builder.advancement()
                        .parent(advancementHolder)
                        .display(
                            MaterialCard.FAIRY_CRYSTAL.item,
                            text { "水晶の飴"() },
                            text { "妖花ミラージュを栽培し希少品を収穫する"() },
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                        )
                        .addCriterion("has_fairy_crystal", InventoryChangeTrigger.TriggerInstance.hasItems(MaterialCard.FAIRY_CRYSTAL.item))
                        .save(writer, MirageFairy2024.identifier("main/a2").string)

                }
            }))
        }
    }

    override fun buildRegistry(registryBuilder: RegistryBuilder) {
        DataGenerationEvents.onBuildRegistry.fire { it(registryBuilder) }
    }
}
