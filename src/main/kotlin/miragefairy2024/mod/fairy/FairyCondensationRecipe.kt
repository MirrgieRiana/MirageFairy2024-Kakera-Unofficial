package miragefairy2024.mod.fairy

import miragefairy2024.ModContext
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.SpecialRecipeResult
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.itemStacks
import miragefairy2024.util.registerSpecialRecipe
import miragefairy2024.util.size
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

context(ModContext)
fun initFairyCondensationRecipe() {
    registerSpecialRecipe("fairy_condensation", 2) { inventory ->
        val itemStacks = inventory.itemStacks

        // 空欄が入っていても無視
        val notEmptyItemStacks = itemStacks.filter { it.isNotEmpty }

        // 余計なアイテムが入っていたら失敗
        notEmptyItemStacks.forEach {
            if (!it.isOf(FairyCard.item)) return@registerSpecialRecipe null
        }

        // 2個以上無ければ失敗
        if (notEmptyItemStacks.size < 2) return@registerSpecialRecipe null

        // 壊れたアイテムだと失敗
        val motifId = notEmptyItemStacks.first().getFairyMotifId() ?: return@registerSpecialRecipe null

        // すべてのモチーフが等しくなければ失敗
        (1 until notEmptyItemStacks.size).forEach { i ->
            if (notEmptyItemStacks[i].getFairyMotifId() != motifId) return@registerSpecialRecipe null
        }

        val condensation = notEmptyItemStacks.sumOf { it.getFairyCondensation().toLong() }
        if (condensation > Integer.MAX_VALUE.toLong()) return@registerSpecialRecipe null

        object : SpecialRecipeResult {
            override fun craft() = createFairyItemStack(motifId, condensation = condensation.toInt())
        }
    }
    registerSpecialRecipe("fairy_decondensation", 1) { inventory ->
        val itemStacks = inventory.itemStacks

        // 空欄無視、インデックス付与
        val entries = itemStacks.mapIndexedNotNull { i, it ->
            if (it.isEmpty) return@mapIndexedNotNull null
            Pair(i, it)
        }

        // アイテムが丁度1個のみのときに反応
        val (index, itemStack) = entries.singleOrNull() ?: return@registerSpecialRecipe null

        // そのアイテムは妖精でなければならない
        if (!itemStack.isOf(FairyCard.item)) return@registerSpecialRecipe null

        // 左上の場合1/10、それ以外の場合、その位置で割る
        val division = if (index == 0) 10 else index + 1

        // 壊れた妖精アイテムは受け付けない
        val motifId = itemStack.getFairyMotifId() ?: return@registerSpecialRecipe null

        // 入力アイテムの凝縮数は、割る数以上でなければならない
        val condensation = itemStack.getFairyCondensation()
        if (condensation < division) return@registerSpecialRecipe null

        val remainingCondensation = condensation % division
        val dividedCondensation = condensation / division

        object : SpecialRecipeResult {
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
}
