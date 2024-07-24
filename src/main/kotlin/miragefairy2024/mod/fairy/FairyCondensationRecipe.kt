package miragefairy2024.mod.fairy

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.itemStacks
import miragefairy2024.util.register
import miragefairy2024.util.size
import miragefairy2024.util.string
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.CraftingRecipe
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

enum class SpecialRecipeCard(path: String, creator: (CraftingRecipeCategory, SpecialRecipeCard) -> CraftingRecipe) {
    FAIRY_CONDENSATION("fairy_condensation", ::FairyCondensationRecipe),
    FAIRY_DECONDENSATION("fairy_decondensation", ::FairyDecondensationRecipe),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val serializer: SpecialRecipeSerializer<*> = SpecialRecipeSerializer { _, category -> creator(category, this) }
}

context(ModContext)
fun initFairyCondensationRecipe() {
    SpecialRecipeCard.entries.forEach { card ->
        card.serializer.register(Registries.RECIPE_SERIALIZER, card.identifier)
        DataGenerationEvents.onGenerateRecipe {
            ComplexRecipeJsonBuilder.create(card.serializer).offerTo(it, card.identifier.string)
        }
    }
}

private class FairyCondensationRecipe(category: CraftingRecipeCategory, val card: SpecialRecipeCard) : SpecialCraftingRecipe(card.identifier, category) {

    private interface MatchResult {
        fun craft(): ItemStack
    }

    private fun match(inventory: RecipeInputInventory): MatchResult? {
        val itemStacks = inventory.itemStacks

        // 空欄が入っていても無視
        val notEmptyItemStacks = itemStacks.filter { it.isNotEmpty }

        // 余計なアイテムが入っていたら失敗
        notEmptyItemStacks.forEach {
            if (!it.isOf(FairyCard.item)) return null
        }

        // 2個以上無ければ失敗
        if (notEmptyItemStacks.size < 2) return null

        // 壊れたアイテムだと失敗
        val motifId = notEmptyItemStacks.first().getFairyMotifId() ?: return null

        // すべてのモチーフが等しくなければ失敗
        (1 until notEmptyItemStacks.size).forEach { i ->
            if (notEmptyItemStacks[i].getFairyMotifId() != motifId) return null
        }

        val condensation = notEmptyItemStacks.sumOf { it.getFairyCondensation().toLong() }
        if (condensation > Integer.MAX_VALUE.toLong()) return null

        return object : MatchResult {
            override fun craft() = createFairyItemStack(motifId, condensation = condensation.toInt())
        }
    }

    override fun matches(inventory: RecipeInputInventory, world: World) = match(inventory) != null
    override fun craft(inventory: RecipeInputInventory, registryManager: DynamicRegistryManager) = match(inventory)?.craft() ?: EMPTY_ITEM_STACK
    override fun fits(width: Int, height: Int) = width * height >= 2
    override fun getSerializer() = card.serializer
}

private class FairyDecondensationRecipe(category: CraftingRecipeCategory, val card: SpecialRecipeCard) : SpecialCraftingRecipe(card.identifier, category) {

    private interface MatchResult {
        fun craft(): ItemStack
        fun getRemainder(): DefaultedList<ItemStack>? = null
    }

    private fun match(inventory: RecipeInputInventory): MatchResult? {
        val itemStacks = inventory.itemStacks

        // 空欄無視、インデックス付与
        val entries = itemStacks.mapIndexedNotNull { i, it ->
            if (it.isEmpty) return@mapIndexedNotNull null
            Pair(i, it)
        }

        // アイテムが丁度1個のみのときに反応
        val (index, itemStack) = entries.singleOrNull() ?: return null

        // そのアイテムは妖精でなければならない
        if (!itemStack.isOf(FairyCard.item)) return null

        // 左上の場合1/10、それ以外の場合、その位置で割る
        val division = if (index == 0) 10 else index + 1

        // 壊れた妖精アイテムは受け付けない
        val motifId = itemStack.getFairyMotifId() ?: return null

        // 入力アイテムの凝縮数は、割る数以上でなければならない
        val condensation = itemStack.getFairyCondensation()
        if (condensation < division) return null

        val remainingCondensation = condensation % division
        val dividedCondensation = condensation / division

        return object : MatchResult {
            override fun craft() = createFairyItemStack(motifId, condensation = dividedCondensation, count = division)
            override fun getRemainder(): DefaultedList<ItemStack>? {
                return if (remainingCondensation > 0) {
                    val list = DefaultedList.ofSize(inventory.size, EMPTY_ITEM_STACK)
                    list[index] = createFairyItemStack(motifId, condensation = remainingCondensation)
                    list
                } else {
                    null
                }
            }
        }
    }

    override fun matches(inventory: RecipeInputInventory, world: World) = match(inventory) != null
    override fun craft(inventory: RecipeInputInventory, registryManager: DynamicRegistryManager) = match(inventory)?.craft() ?: EMPTY_ITEM_STACK
    override fun getRemainder(inventory: RecipeInputInventory): DefaultedList<ItemStack> = match(inventory)?.getRemainder() ?: DefaultedList.ofSize(inventory.size, EMPTY_ITEM_STACK)
    override fun fits(width: Int, height: Int) = width * height >= 1
    override fun getSerializer() = card.serializer
}
