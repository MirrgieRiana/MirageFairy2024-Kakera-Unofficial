package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.recipeGroupRegistry
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.block.Blocks
import net.minecraft.block.ComposterBlock
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.loot.condition.KilledByPlayerLootCondition
import net.minecraft.loot.condition.LocationCheckLootCondition
import net.minecraft.loot.condition.MatchToolLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.condition.RandomChanceWithLootingLootCondition
import net.minecraft.loot.entry.LeafEntry
import net.minecraft.loot.function.ApplyBonusLootFunction
import net.minecraft.loot.function.ExplosionDecayLootFunction
import net.minecraft.loot.function.LootingEnchantLootFunction
import net.minecraft.loot.function.SetCountLootFunction
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.predicate.entity.LocationPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

// Crafting

fun <T : CraftingRecipeJsonBuilder> T.criterion(item: Item) = this.also { it.criterion("has_${item.getIdentifier().path}", RecipeProvider.conditionsFromItem(item)) }
fun <T : CraftingRecipeJsonBuilder> T.criterion(tagKey: TagKey<Item>) = this.also { it.criterion("has_${tagKey.id.path}", RecipeProvider.conditionsFromTag(tagKey)) }
fun <T : CraftingRecipeJsonBuilder> T.group(item: Item) = this.also { it.group(recipeGroupRegistry[item] ?: "${item.getIdentifier()}") }

class RecipeGenerationSettings<T> {
    val listeners = mutableListOf<(T) -> Unit>()
    val idModifiers = mutableListOf<(Identifier) -> Identifier>()
    var recipeCategory = RecipeCategory.MISC
    var noGroup = false
}

infix fun <T : CraftingRecipeJsonBuilder> RecipeGenerationSettings<T>.on(item: Item) = this.apply {
    this.listeners += { it.criterion(item) }
}

infix fun <T> RecipeGenerationSettings<T>.modId(modId: String) = this.apply {
    this.idModifiers += { Identifier(modId, it.path) }
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
    ModEvents.onInitialize {
        DataGenerationEvents.recipeGenerators {
            val builder = creator(settings.recipeCategory, item, count)
            settings.listeners.forEach { listener ->
                listener(builder)
            }
            if (!settings.noGroup) builder.group(item)
            block(builder)
            val identifier = settings.idModifiers.fold(item.getIdentifier()) { id, idModifier -> idModifier(id) }
            builder.offerTo(it, identifier)
        }
    }
    return settings
}

context(ModContext)
fun registerShapedRecipeGeneration(
    item: Item,
    count: Int = 1,
    block: ShapedRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<ShapedRecipeJsonBuilder> = registerRecipeGeneration(ShapedRecipeJsonBuilder::create, item, count, block)

context(ModContext)
fun registerShapelessRecipeGeneration(
    item: Item,
    count: Int = 1,
    block: ShapelessRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<ShapelessRecipeJsonBuilder> = registerRecipeGeneration(ShapelessRecipeJsonBuilder::create, item, count, block)

context(ModContext)
fun registerSmeltingRecipeGeneration(
    input: Item,
    output: Item,
    experience: Double = 0.0,
    cookingTime: Int = 200,
    block: CookingRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<CookingRecipeJsonBuilder> {
    val settings = RecipeGenerationSettings<CookingRecipeJsonBuilder>()
    ModEvents.onInitialize {
        DataGenerationEvents.recipeGenerators {
            val builder = CookingRecipeJsonBuilder.createSmelting(Ingredient.ofItems(input), RecipeCategory.MISC, output, experience.toFloat(), cookingTime)
            builder.group(output)
            settings.listeners.forEach { listener ->
                listener(builder)
            }
            block(builder)
            val identifier = settings.idModifiers.fold(output.getIdentifier()) { id, idModifier -> idModifier(id) }
            builder.offerTo(it, identifier)
        }
    }
    return settings
}

context(ModContext)
fun registerBlastingRecipeGeneration(
    input: Item,
    output: Item,
    experience: Double = 0.0,
    cookingTime: Int = 100,
    block: CookingRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<CookingRecipeJsonBuilder> {
    val settings = RecipeGenerationSettings<CookingRecipeJsonBuilder>()
    ModEvents.onInitialize {
        DataGenerationEvents.recipeGenerators {
            val builder = CookingRecipeJsonBuilder.createBlasting(Ingredient.ofItems(input), RecipeCategory.MISC, output, experience.toFloat(), cookingTime)
            builder.group(output)
            settings.listeners.forEach { listener ->
                listener(builder)
            }
            block(builder)
            val identifier = settings.idModifiers.fold(output.getIdentifier() * "_from_blasting") { id, idModifier -> idModifier(id) }
            builder.offerTo(it, identifier)
        }
    }
    return settings
}


// Others

context(ModContext)
fun Item.registerGrassDrop(amount: Float = 1.0F, fortuneMultiplier: Int = 2, biome: (() -> RegistryKey<Biome>)? = null) = ModEvents.onInitialize {
    LootTableEvents.MODIFY.register { _, _, id, tableBuilder, source ->
        if (source.isBuiltin) {
            if (id == Blocks.GRASS.lootTableId) {
                tableBuilder.configure {
                    pool(LootPool(AlternativeLootPoolEntry {
                        alternatively(EmptyLootPoolEntry {
                            conditionally(MatchToolLootCondition.builder(ItemPredicate.Builder.create().items(Items.SHEARS)))
                        })
                        alternatively(ItemLootPoolEntry(this@registerGrassDrop) {
                            conditionally(RandomChanceLootCondition.builder(0.125F * amount))
                            if (biome != null) conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(biome())))
                            apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE, fortuneMultiplier))
                            apply(ExplosionDecayLootFunction.builder())
                        })
                    }))
                }
            }
        }
    }
}

context(ModContext)
fun Item.registerMobDrop(
    entityType: EntityType<*>,
    onlyKilledByPlayer: Boolean = false,
    dropRate: Pair<Float, Float>? = null,
    amount: LootNumberProvider? = null,
    fortuneFactor: LootNumberProvider? = null,
) = ModEvents.onInitialize {
    val lootTableId = entityType.lootTableId
    LootTableEvents.MODIFY.register { _, _, id, tableBuilder, source ->
        if (source.isBuiltin) {
            if (id == lootTableId) {
                tableBuilder.configure {
                    pool(LootPool(ItemLootPoolEntry(this@registerMobDrop) {
                        if (amount != null) apply(SetCountLootFunction.builder(amount, false))
                        if (fortuneFactor != null) apply(LootingEnchantLootFunction.builder(fortuneFactor))
                    }) {
                        if (onlyKilledByPlayer) conditionally(KilledByPlayerLootCondition.builder())
                        if (dropRate != null) conditionally(RandomChanceWithLootingLootCondition.builder(dropRate.first, dropRate.second))
                    })
                }
            }
        }
    }
}

context(ModContext)
fun Item.registerChestLoot(lootTableId: Identifier, weight: Int = 10, count: IntRange? = null, block: LeafEntry.Builder<*>.() -> Unit = {}) = ModEvents.onInitialize {
    LootTableEvents.MODIFY.register { _, _, id, tableBuilder, source ->
        if (source.isBuiltin) {
            if (id == lootTableId) {
                tableBuilder.modifyPools { lootPool ->
                    lootPool.configure {
                        with(ItemLootPoolEntry(this@registerChestLoot) {
                            weight(weight)
                            if (count != null) apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(count.first.toFloat(), count.last.toFloat())))
                            block(this)
                        })
                    }
                }
            }
        }
    }
}

context(ModContext)
fun Item.registerComposterInput(chance: Float) = ModEvents.onInitialize {
    ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(this, chance)
}

/** @param ticks coal is `200 * 8 = 1600` */
context(ModContext)
fun Item.registerFuel(ticks: Int) = ModEvents.onInitialize {
    FuelRegistry.INSTANCE.add(this, ticks)
}
