package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.util.Translation
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

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
