package miragefairy2024

import com.google.gson.JsonElement
import miragefairy2024.mod.NinePatchTextureCard
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.storage.loot.LootTable
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

object ModEvents {
    val onInitialize = InitializationEventRegistry<() -> Unit>()
    val onClientInit = InitializationEventRegistry<() -> Unit>()
    val onTerraBlenderInitialized = InitializationEventRegistry<() -> Unit>()
}

object DataGenerationEvents {
    val onInitializeDataGenerator = InitializationEventRegistry<() -> Unit>()

    val onGenerateBlockModel = InitializationEventRegistry<(BlockModelGenerators) -> Unit>()
    val onGenerateItemModel = InitializationEventRegistry<(ItemModelGenerators) -> Unit>()
    val onGenerateBlockLootTable = InitializationEventRegistry<(FabricBlockLootTableProvider, HolderLookup.Provider) -> Unit>()
    val onGenerateChestLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateArchaeologyLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateEntityLootTable = InitializationEventRegistry<((EntityType<*>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateAdvancementRewardLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateRecipe = InitializationEventRegistry<(RecipeOutput) -> Unit>()
    val onGenerateEnglishTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateJapaneseTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateNinePatchTexture = InitializationEventRegistry<((ResourceLocation, NinePatchTextureCard) -> Unit) -> Unit>()
    val onGenerateSound = InitializationEventRegistry<((path: String, subtitle: String?, sounds: List<ResourceLocation>) -> Unit) -> Unit>()
    val onGenerateParticles = InitializationEventRegistry<((identifier: ResourceLocation, jsonElement: JsonElement) -> Unit) -> Unit>()
    val onGenerateAdvancement = InitializationEventRegistry<suspend (HolderLookup.Provider, Consumer<AdvancementHolder>) -> Unit>()
    val onGenerateDataMap = InitializationEventRegistry<(DataMapConsumer, HolderLookup.Provider) -> Unit>()

    val onBuildRegistry = InitializationEventRegistry<(RegistrySetBuilder) -> Unit>()

    val dynamicGenerationRegistries = mutableSetOf<ResourceKey<out Registry<*>>>()
}

class TagGenerator<T>(
    private val registryKey: ResourceKey<out Registry<T>>,
    private val reverseLookupFunction: ((T) -> ResourceKey<T>)? = null,
) {
    companion object {
        val entries = mutableListOf<TagGenerator<*>>()
        private operator fun <T> TagGenerator<T>.not() = this.also { entries.add(it) }

        val BLOCK = !TagGenerator(Registries.BLOCK) { element -> element.builtInRegistryHolder().key() }
        val ITEM = !TagGenerator(Registries.ITEM) { element -> element.builtInRegistryHolder().key() }
        val BIOME = !TagGenerator(Registries.BIOME)
        val STRUCTURE = !TagGenerator(Registries.STRUCTURE)
        val ENTITY_TYPE = !TagGenerator(Registries.ENTITY_TYPE)
        val DAMAGE_TYPE = !TagGenerator(Registries.DAMAGE_TYPE)
        val ENCHANTMENT = !TagGenerator(Registries.ENCHANTMENT)
    }

    val eventRegistry = InitializationEventRegistry<((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit>()

    fun createProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>): FabricTagProvider<T> {
        return object : FabricTagProvider<T>(output, registryKey, registriesFuture) {
            override fun reverseLookup(element: T) = if (reverseLookupFunction == null) super.reverseLookup(element) else reverseLookupFunction(element)
            override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
        }
    }
}

interface DataMapConsumer {
    fun <T> accept(registry: ResourceKey<Registry<T>>, type: ResourceLocation, target: ResourceKey<T>, data: JsonElement)
}

object MirageFairy2024 {
    const val MOD_ID = "miragefairy2024"
    fun identifier(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
}
