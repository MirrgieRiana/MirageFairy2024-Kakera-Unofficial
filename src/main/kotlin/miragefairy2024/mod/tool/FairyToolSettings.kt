package miragefairy2024.mod.tool

import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.translation
import miragefairy2024.util.Translation
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey

class FairyMiningToolSettings(
    val toolMaterialCard: ToolMaterialCard,
) : ToolSettings<FairyMiningToolItem> {

    val tags = mutableListOf<TagKey<Item>>()
    var attackDamage = 0F
    var attackSpeed = 0F
    val effectiveBlockTags = mutableListOf<TagKey<Block>>()
    var areaMining = false
    var mineAll = false
    var cutAll = false
    var silkTouch = false
    var selfMending: Int? = null
    val descriptions = mutableListOf<Translation>()

    override fun createItem() = FairyMiningToolItem(this, Item.Settings())

    context(ModContext)
    override fun init(card: ToolCard<FairyMiningToolItem>) {
        tags.forEach {
            card.item.registerItemTagGeneration { it }
        }
        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }

    override fun addPoems(poemList: PoemList) = descriptions.fold(poemList) { it, description -> it.translation(PoemType.DESCRIPTION, description) }

}


// Sword 3, -2.4

// Shovel 1.5, -3.0

fun createPickaxe(toolMaterialCard: ToolMaterialCard) = FairyMiningToolSettings(toolMaterialCard).also {
    it.attackDamage = 1F
    it.attackSpeed = -2.8F
    it.tags += ItemTags.PICKAXES
    it.tags += ItemTags.CLUSTER_MAX_HARVESTABLES
    it.effectiveBlockTags += BlockTags.PICKAXE_MINEABLE
}

/**
 * @param attackDamage wood: 6.0, stone: 7.0, gold: 6.0, iron: 6.0, diamond: 5.0, netherite: 5.0
 * @param attackSpeed wood: -3.2, stone: -3.2, gold: -3.0, iron: -3.1, diamond: -3.0, netherite: -3.0
 */
fun createAxe(toolMaterialCard: ToolMaterialCard, attackDamage: Float, attackSpeed: Float) = FairyMiningToolSettings(toolMaterialCard).also {
    it.attackDamage = attackDamage
    it.attackSpeed = attackSpeed
    it.tags += ItemTags.AXES
    it.effectiveBlockTags += BlockTags.AXE_MINEABLE
}

// Hoe
// @param attackDamage wood: 0.0, stone: -1.0, gold: 0.0, iron: -2.0, diamond: -3.0, netherite: -4.0
// @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0

fun FairyMiningToolSettings.areaMining() = this.also {
    it.areaMining = true
    it.descriptions += FairyMiningToolItem.AREA_MINING_TRANSLATION
}

fun FairyMiningToolSettings.mineAll() = this.also {
    it.mineAll = true
    it.descriptions += FairyMiningToolItem.MINE_ALL_TRANSLATION
}

fun FairyMiningToolSettings.cutAll() = this.also {
    it.cutAll = true
    it.descriptions += FairyMiningToolItem.CUT_ALL_TRANSLATION
}

fun FairyMiningToolSettings.silkTouch() = this.also {
    it.silkTouch = true
    it.descriptions += FairyMiningToolItem.SILK_TOUCH_TRANSLATION
}

fun FairyMiningToolSettings.selfMending(selfMending: Int) = this.also {
    it.selfMending = selfMending
    it.descriptions += FairyMiningToolItem.SELF_MENDING_TRANSLATION
}
