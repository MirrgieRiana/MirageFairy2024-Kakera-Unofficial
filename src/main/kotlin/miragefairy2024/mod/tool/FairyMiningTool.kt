package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.translation
import miragefairy2024.util.Translation
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

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


class FairyMiningToolItem(val toolSettings: FairyMiningToolSettings, settings: Settings) : MiningToolItem(toolSettings.attackDamage, toolSettings.attackSpeed, toolSettings.toolMaterialCard.toolMaterial, BlockTags.PICKAXE_MINEABLE/* dummy */, settings), OverrideEnchantmentLevelCallback, ItemPredicateConvertorCallback {
    companion object {
        val AREA_MINING_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.area_mining" }, "Area mining", "範囲採掘")
        val MINE_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")
        val CUT_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.cut_all" }, "Cut down the entire tree", "木全体を伐採")
        val SILK_TOUCH_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.silk_touch" }, "Silk Touch", "シルクタッチ")
        val SELF_MENDING_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.self_mending" }, "Self-mending while in the main hand", "メインハンドにある間、自己修繕")
    }

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(stack, state)
    override fun isSuitableFor(state: BlockState) = isSuitableForImpl(state)

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.postMine(stack, world, state, pos, miner)
        postMineImpl(stack, world, state, pos, miner)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) = inventoryTickImpl(stack, world, entity, slot, selected)

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = overrideEnchantmentLevelImpl(enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(itemStack)
}
