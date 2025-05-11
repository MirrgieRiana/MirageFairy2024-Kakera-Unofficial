package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.recipeGroupRegistry
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.advancements.critereon.EnchantmentPredicate
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.ItemSubPredicates
import net.minecraft.advancements.critereon.LocationPredicate
import net.minecraft.core.HolderLookup
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ComposterBlock
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition
import net.minecraft.advancements.critereon.MinMaxBounds as NumberRange
import net.minecraft.core.NonNullList as DefaultedList
import net.minecraft.data.recipes.RecipeBuilder as CraftingRecipeJsonBuilder
import net.minecraft.data.recipes.ShapedRecipeBuilder as ShapedRecipeJsonBuilder
import net.minecraft.data.recipes.ShapelessRecipeBuilder as ShapelessRecipeJsonBuilder
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder as CookingRecipeJsonBuilder
import net.minecraft.data.recipes.SpecialRecipeBuilder as ComplexRecipeJsonBuilder
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer as SpecialRecipeSerializer
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer as LeafEntry
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount as ApplyBonusLootFunction
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay as ExplosionDecayLootFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction as SetCountLootFunction
import net.minecraft.world.level.storage.loot.predicates.LocationCheck as LocationCheckLootCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition as KilledByPlayerLootCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition as RandomChanceLootCondition
import net.minecraft.world.level.storage.loot.predicates.MatchTool as MatchToolLootCondition
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider as LootNumberProvider
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator as UniformLootNumberProvider

// Crafting

fun <T : CraftingRecipeJsonBuilder> T.criterion(item: Item) = this.also { it.unlockedBy("has_${item.getIdentifier().path}", RecipeProvider.has(item)) }
fun <T : CraftingRecipeJsonBuilder> T.criterion(tagKey: TagKey<Item>) = this.also { it.unlockedBy("has_${tagKey.location().path}", RecipeProvider.has(tagKey)) }
fun <T : CraftingRecipeJsonBuilder> T.group(item: Item) = this.also { it.group(recipeGroupRegistry[item] ?: "${item.getIdentifier()}") }

class RecipeGenerationSettings<T> {
    val listeners = mutableListOf<(T) -> Unit>()
    val idModifiers = mutableListOf<(ResourceLocation) -> ResourceLocation>()
    var recipeCategory = RecipeCategory.MISC
    var noGroup = false
}

infix fun <T : CraftingRecipeJsonBuilder> RecipeGenerationSettings<T>.on(item: Item) = this.apply {
    this.listeners += { it.criterion(item) }
}

infix fun <T> RecipeGenerationSettings<T>.modId(modId: String) = this.apply {
    this.idModifiers += { ResourceLocation.fromNamespaceAndPath(modId, it.path) }
}

infix fun <T> RecipeGenerationSettings<T>.from(item: Item) = this.apply {
    this.idModifiers += { it * "_from_" * item.getIdentifier().path }
}

fun <T> RecipeGenerationSettings<T>.noGroup(noGroup: Boolean = true) = this.apply {
    this.noGroup = noGroup
}

context(ModContext)
fun <T : CraftingRecipeJsonBuilder> registerRecipeGeneration(
    creator: (RecipeCategory, Item, Int) -> T,
    item: Item,
    count: Int = 1,
    block: T.() -> Unit = {},
): RecipeGenerationSettings<T> {
    val settings = RecipeGenerationSettings<T>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = creator(settings.recipeCategory, item, count)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        if (!settings.noGroup) builder.group(item)
        block(builder)
        val identifier = settings.idModifiers.fold(item.getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}

context(ModContext)
fun registerShapedRecipeGeneration(
    item: Item,
    count: Int = 1,
    block: ShapedRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<ShapedRecipeJsonBuilder> = registerRecipeGeneration(ShapedRecipeJsonBuilder::shaped, item, count, block)

context(ModContext)
fun registerShapelessRecipeGeneration(
    item: Item,
    count: Int = 1,
    block: ShapelessRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<ShapelessRecipeJsonBuilder> = registerRecipeGeneration(ShapelessRecipeJsonBuilder::shapeless, item, count, block)

context(ModContext)
fun registerSmeltingRecipeGeneration(
    input: Item,
    output: Item,
    experience: Double = 0.0,
    cookingTime: Int = 200,
    block: CookingRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<CookingRecipeJsonBuilder> {
    val settings = RecipeGenerationSettings<CookingRecipeJsonBuilder>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = CookingRecipeJsonBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, output, experience.toFloat(), cookingTime)
        builder.group(output)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(output.getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}

context(ModContext)
fun registerBlastingRecipeGeneration(
    input: Item,
    output: Item,
    experience: Double = 0.0,
    cookingTime: Int = 100,
    block: CookingRecipeJsonBuilder. () -> Unit = {},
): RecipeGenerationSettings<CookingRecipeJsonBuilder> {
    val settings = RecipeGenerationSettings<CookingRecipeJsonBuilder>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = CookingRecipeJsonBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, output, experience.toFloat(), cookingTime)
        builder.group(output)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(output.getIdentifier() * "_from_blasting") { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}


// Special Recipe

context(ModContext)
fun registerSpecialRecipe(path: String, minSlots: Int, matcher: (CraftingInput) -> SpecialRecipeResult?) {
    val identifier = MirageFairy2024.identifier(path)
    lateinit var serializer: SpecialRecipeSerializer<*>

    class SpecialCraftingRecipeImpl(category: CraftingBookCategory) : CustomRecipe(category) {
        override fun matches(input: CraftingInput, world: Level) = matcher(input) != null
        override fun assemble(input: CraftingInput, registries: HolderLookup.Provider) = matcher(input)?.craft() ?: EMPTY_ITEM_STACK
        override fun getRemainingItems(input: CraftingInput) = matcher(input)?.getRemainder() ?: super.getRemainingItems(input)
        override fun canCraftInDimensions(width: Int, height: Int) = width * height >= minSlots
        override fun getSerializer() = serializer
    }

    serializer = SpecialRecipeSerializer(::SpecialCraftingRecipeImpl)
    BuiltInRegistries.RECIPE_SERIALIZER.register(identifier) { serializer }
    DataGenerationEvents.onGenerateRecipe {
        ComplexRecipeJsonBuilder.special(::SpecialCraftingRecipeImpl).save(it, identifier.string)
    }
}

interface SpecialRecipeResult {
    fun craft(): ItemStack
    fun getRemainder(): DefaultedList<ItemStack>? = null
}

fun MutableList<ItemStack>.pull(predicate: (ItemStack) -> Boolean): ItemStack? {
    val index = this.indexOfFirst { predicate(it) }
    if (index == -1) return null
    return this.removeAt(index)
}


// Others

context(ModContext)
fun registerCompressionRecipeGeneration(lowerItem: Item, higherItem: Item, count: Int = 9, noGroup: Boolean = false) {
    registerShapelessRecipeGeneration(higherItem, count = 1) {
        repeat(count) {
            requires(lowerItem)
        }
    }.noGroup(noGroup) on lowerItem from lowerItem
    registerShapelessRecipeGeneration(lowerItem, count = count) {
        requires(higherItem)
    }.noGroup(noGroup) on higherItem from higherItem
}

context(ModContext)
fun Registration<Item>.registerLootTableModification(lootTableIdGetter: () -> ResourceKey<LootTable>, block: (LootTable.Builder, HolderLookup.Provider) -> Unit) = ModEvents.onInitialize {
    val lootTableId = lootTableIdGetter()
    LootTableEvents.MODIFY.register { id, tableBuilder, source, registries ->
        if (source.isBuiltin) {
            if (id == lootTableId) {
                block(tableBuilder, registries)
            }
        }
    }
}

context(ModContext)
fun Registration<Item>.registerGrassDrop(
    amount: Float = 1.0F,
    fortuneMultiplier: Int = 2,
    biome: (() -> ResourceKey<Biome>)? = null,
) = this.registerLootTableModification({ Blocks.SHORT_GRASS.lootTable }) { tableBuilder, registries ->
    tableBuilder.configure {
        withPool(LootPool(AlternativeLootPoolEntry {
            otherwise(EmptyLootPoolEntry {
                `when`(MatchToolLootCondition.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)))
            })
            otherwise(ItemLootPoolEntry(this@registerGrassDrop()) {
                `when`(RandomChanceLootCondition.randomChance(0.125F * amount))
                if (biome != null) `when`(LocationCheckLootCondition.checkLocation(LocationPredicate.Builder.location().setBiomes(HolderSet.direct(registries[Registries.BIOME, biome()]))))
                apply(ApplyBonusLootFunction.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], fortuneMultiplier))
                apply(ExplosionDecayLootFunction.explosionDecay())
            })
        }))
    }
}

context(ModContext)
fun Registration<Item>.registerExtraOreDrop(
    oreBlock: Block,
    chance: Float = 1.0F,
    fortuneMultiplier: Int = 0,
) = this.registerLootTableModification({ oreBlock.lootTable }) { tableBuilder, registries ->
    tableBuilder.configure {
        withPool(LootPool(AlternativeLootPoolEntry {
            otherwise(EmptyLootPoolEntry {
                `when`(MatchToolLootCondition.toolMatches(ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.enchantments(listOf(EnchantmentPredicate(registries[Registries.ENCHANTMENT, Enchantments.SILK_TOUCH], NumberRange.Ints.atLeast(1)))))))
            })
            otherwise(ItemLootPoolEntry(this@registerExtraOreDrop()) {
                if (chance < 1.0F) `when`(RandomChanceLootCondition.randomChance(chance))
                if (fortuneMultiplier > 0) apply(ApplyBonusLootFunction.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], fortuneMultiplier))
                apply(ExplosionDecayLootFunction.explosionDecay())
            })
        }))
    }
}

context(ModContext)
fun Registration<Item>.registerMobDrop(
    entityType: EntityType<*>,
    onlyKilledByPlayer: Boolean = false,
    dropRate: Pair<Float, Float>? = null,
    amount: LootNumberProvider? = null,
    fortuneFactor: LootNumberProvider? = null,
) = this.registerLootTableModification({ entityType.defaultLootTable }) { tableBuilder, registries ->
    tableBuilder.configure {
        withPool(LootPool(ItemLootPoolEntry(this@registerMobDrop()) {
            if (amount != null) apply(SetCountLootFunction.setCount(amount, false))
            if (fortuneFactor != null) apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, fortuneFactor))
        }) {
            if (onlyKilledByPlayer) `when`(KilledByPlayerLootCondition.killedByPlayer())
            if (dropRate != null) `when`(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, dropRate.first, dropRate.second))
        })
    }
}

context(ModContext)
fun Registration<Item>.registerChestLoot(
    lootTableIdGetter: () -> ResourceKey<LootTable>,
    weight: Int = 10,
    count: IntRange? = null,
    block: LeafEntry.Builder<*>.() -> Unit = {},
) = this.registerLootTableModification(lootTableIdGetter) { tableBuilder, registries ->
    tableBuilder.modifyPools { lootPool ->
        lootPool.configure {
            add(ItemLootPoolEntry(this@registerChestLoot()) {
                setWeight(weight)
                if (count != null) apply(SetCountLootFunction.setCount(UniformLootNumberProvider.between(count.first.toFloat(), count.last.toFloat())))
                block(this)
            })
        }
    }
}

context(ModContext)
fun Item.registerComposterInput(chance: Float) = ModEvents.onInitialize {
    ComposterBlock.COMPOSTABLES.put(this, chance)
}

/** @param ticks coal is `200 * 8 = 1600` */
context(ModContext)
fun Registration<Item>.registerFuel(ticks: Int) = ModEvents.onInitialize {
    FuelRegistry.INSTANCE.add(this(), ticks)
}
