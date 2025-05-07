package miragefairy2024

import com.google.gson.JsonElement
import miragefairy2024.mod.NinePatchTextureCard
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.storage.loot.LootTable

object ModEvents {
    val onRegistration = InitializationEventRegistry<() -> Unit>()
    val onInitialize = InitializationEventRegistry<() -> Unit>()

    val onClientInit = InitializationEventRegistry<() -> Unit>()
}

object DataGenerationEvents {
    val onInitializeDataGenerator = InitializationEventRegistry<() -> Unit>()

    val onGenerateBlockStateModel = InitializationEventRegistry<(BlockModelGenerators) -> Unit>()
    val onGenerateItemModel = InitializationEventRegistry<(ItemModelGenerators) -> Unit>()
    val onGenerateBlockTag = InitializationEventRegistry<((TagKey<Block>) -> FabricTagProvider<Block>.FabricTagBuilder) -> Unit>()
    val onGenerateItemTag = InitializationEventRegistry<((TagKey<Item>) -> FabricTagProvider<Item>.FabricTagBuilder) -> Unit>()
    val onGenerateBiomeTag = InitializationEventRegistry<((TagKey<Biome>) -> FabricTagProvider<Biome>.FabricTagBuilder) -> Unit>()
    val onGenerateStructureTag = InitializationEventRegistry<((TagKey<Structure>) -> FabricTagProvider<Structure>.FabricTagBuilder) -> Unit>()
    val onGenerateEntityTypeTag = InitializationEventRegistry<((TagKey<EntityType<*>>) -> FabricTagProvider<EntityType<*>>.FabricTagBuilder) -> Unit>()
    val onGenerateDamageTypeTag = InitializationEventRegistry<((TagKey<DamageType>) -> FabricTagProvider<DamageType>.FabricTagBuilder) -> Unit>()
    val onGenerateEnchantmentTag = InitializationEventRegistry<((TagKey<Enchantment>) -> FabricTagProvider<Enchantment>.FabricTagBuilder) -> Unit>()
    val onGenerateBlockLootTable = InitializationEventRegistry<(FabricBlockLootTableProvider, HolderLookup.Provider) -> Unit>()
    val onGenerateChestLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateArchaeologyLootTable = InitializationEventRegistry<((ResourceKey<LootTable>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateEntityLootTable = InitializationEventRegistry<((EntityType<*>, LootTable.Builder) -> Unit, HolderLookup.Provider) -> Unit>()
    val onGenerateRecipe = InitializationEventRegistry<(RecipeOutput) -> Unit>()
    val onGenerateEnglishTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateJapaneseTranslation = InitializationEventRegistry<(FabricLanguageProvider.TranslationBuilder) -> Unit>()
    val onGenerateNinePatchTexture = InitializationEventRegistry<((ResourceLocation, NinePatchTextureCard) -> Unit) -> Unit>()
    val onGenerateSound = InitializationEventRegistry<((path: String, subtitle: String?, sounds: List<ResourceLocation>) -> Unit) -> Unit>()
    val onGenerateParticles = InitializationEventRegistry<((identifier: ResourceLocation, jsonElement: JsonElement) -> Unit) -> Unit>()

    val onBuildRegistry = InitializationEventRegistry<(RegistrySetBuilder) -> Unit>()

    val dynamicGenerationRegistries = mutableSetOf<ResourceKey<out Registry<*>>>()
}

object MirageFairy2024 {
    const val MOD_ID = "miragefairy2024"
    fun identifier(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
}
