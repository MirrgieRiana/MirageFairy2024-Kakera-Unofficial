package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.translation
import miragefairy2024.util.NeighborType
import miragefairy2024.util.Translation
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.randomInt
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.repair
import mirrg.kotlin.hydrogen.atLeast
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.fabricmc.yarn.constants.MiningLevels
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FairyMiningToolType(
    val toolMaterialCard: ToolMaterialCard,
) : ToolType<FairyMiningToolItem> {

    val tags = mutableListOf<TagKey<Item>>()
    var attackDamage = 0F
    var attackSpeed = 0F
    val effectiveBlockTags = mutableListOf<TagKey<Block>>()
    var areaMining = false
    var mineAll = false
    var cutAll = false
    var silkTouch = false
    var selfMending = false
    val descriptions = mutableListOf<Translation>()

    override fun createItem() = FairyMiningToolItem(this, Item.Settings())

    override fun init(card: ToolCard<FairyMiningToolItem>) = ModEvents.onInitialize {
        tags.forEach {
            card.item.registerItemTagGeneration { it }
        }
        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }

    override fun addPoems(poemList: PoemList) = descriptions.fold(poemList) { it, description -> it.translation(PoemType.DESCRIPTION, description) }

}

// Sword 3, -2.4

// Shovel 1.5, -3.0

fun FairyMiningToolType.pickaxe() = this.also {
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
fun FairyMiningToolType.axe(attackDamage: Float, attackSpeed: Float) = this.also {
    it.attackDamage = attackDamage
    it.attackSpeed = attackSpeed
    it.tags += ItemTags.AXES
    it.effectiveBlockTags += BlockTags.AXE_MINEABLE
}

// Hoe
// @param attackDamage wood: 0.0, stone: -1.0, gold: 0.0, iron: -2.0, diamond: -3.0, netherite: -4.0
// @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0

fun FairyMiningToolType.areaMining() = this.also {
    it.areaMining = true
    it.descriptions += FairyMiningToolItem.AREA_MINING_TRANSLATION
}

fun FairyMiningToolType.mineAll() = this.also {
    it.mineAll = true
    it.descriptions += FairyMiningToolItem.MINE_ALL_TRANSLATION
}

fun FairyMiningToolType.cutAll() = this.also {
    it.cutAll = true
    it.descriptions += FairyMiningToolItem.CUT_ALL_TRANSLATION
}

fun FairyMiningToolType.silkTouch() = this.also {
    it.silkTouch = true
    it.descriptions += FairyMiningToolItem.SILK_TOUCH_TRANSLATION
}

fun FairyMiningToolType.selfMending() = this.also {
    it.selfMending = true
    it.descriptions += FairyMiningToolItem.SELF_MENDING_TRANSLATION
}


class FairyMiningToolItem(private val type: FairyMiningToolType, settings: Settings) : MiningToolItem(type.attackDamage, type.attackSpeed, type.toolMaterialCard.toolMaterial, BlockTags.PICKAXE_MINEABLE/* dummy */, settings), OverrideEnchantmentLevelCallback, ItemPredicateConvertorCallback {
    companion object {
        val AREA_MINING_TRANSLATION = Translation({ "item.${MirageFairy2024.modId}.fairy_mining_tool.area_mining" }, "Area mining", "範囲採掘")
        val MINE_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.modId}.fairy_mining_tool.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")
        val CUT_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.modId}.fairy_mining_tool.cut_all" }, "Cut down the entire tree", "木全体を伐採")
        val SILK_TOUCH_TRANSLATION = Translation({ "item.${MirageFairy2024.modId}.fairy_mining_tool.silk_touch" }, "Silk Touch", "シルクタッチ")
        val SELF_MENDING_TRANSLATION = Translation({ "item.${MirageFairy2024.modId}.fairy_mining_tool.self_mending" }, "Self-mending while in the main hand", "メインハンドにある間、自己修繕")
    }

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState) = if (type.effectiveBlockTags.any { state.isIn(it) }) miningSpeed else 1.0F
    override fun isSuitableFor(state: BlockState): Boolean {
        val itemMiningLevel = material.miningLevel
        return when {
            itemMiningLevel < MiningLevels.DIAMOND && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) -> false
            itemMiningLevel < MiningLevels.IRON && state.isIn(BlockTags.NEEDS_IRON_TOOL) -> false
            itemMiningLevel < MiningLevels.STONE && state.isIn(BlockTags.NEEDS_STONE_TOOL) -> false
            else -> type.effectiveBlockTags.any { state.isIn(it) }
        }
    }

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.postMine(stack, world, state, pos, miner)
        if (type.areaMining) run fail@{
            if (world.isClient) return@fail

            if (miner.isSneaking) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない

            // 発動

            val baseHardness = state.getHardness(world, pos)

            (-1..1).forEach { x ->
                (-1..1).forEach { y ->
                    (-1..1).forEach { z ->
                        if (x != 0 || y != 0 || z != 0) {
                            val targetBlockPos = pos.add(x, y, z)
                            if (isSuitableFor(world.getBlockState(targetBlockPos))) run skip@{
                                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                                if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

                                // 採掘を続行

                                val targetBlockState = world.getBlockState(targetBlockPos)
                                val targetHardness = targetBlockState.getHardness(world, targetBlockPos)
                                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                                if (breakBlockByMagic(stack, world, targetBlockPos, miner)) {
                                    if (targetHardness > 0) {
                                        stack.damage(1, miner) {
                                            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (type.mineAll) run fail@{
            if (world.isClient) return@fail

            if (miner.isSneaking) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
            if (!state.isIn(ConventionalBlockTags.ORES)) return@fail // 掘ったブロックが鉱石ではない

            // 発動

            val baseHardness = state.getHardness(world, pos)

            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 31) { _, toBlockPos ->
                world.getBlockState(toBlockPos).block === state.block
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getHardness(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        stack.damage(1, miner) {
                            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                        }
                    }
                }
            }
        }
        if (type.cutAll) run fail@{
            if (world.isClient) return@fail

            if (miner.isSneaking) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
            if (!state.isIn(BlockTags.LOGS)) return@fail // 掘ったブロックが原木ではない

            // 発動

            val baseHardness = state.getHardness(world, pos)

            val logBlockPosList = mutableListOf<BlockPos>()
            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 19, neighborType = NeighborType.VERTICES) { _, toBlockPos ->
                world.getBlockState(toBlockPos).isIn(BlockTags.LOGS)
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getHardness(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        stack.damage(1, miner) {
                            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                        }
                    }
                    logBlockPosList += blockPos
                }
            }
            blockVisitor(logBlockPosList, visitOrigins = false, maxDistance = 8) { _, toBlockPos ->
                world.getBlockState(toBlockPos).isIn(BlockTags.LEAVES)
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getHardness(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        if (miner.random.nextFloat() < 0.1F) {
                            stack.damage(1, miner) {
                                it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (type.selfMending) run {
            if (world.isClient) return@run
            if (entity !is PlayerEntity) return@run // プレイヤーじゃない
            if (stack !== entity.mainHandStack) return@run // メインハンドに持っていない
            stack.repair(world.random.randomInt(1.0 / 60.0 / 20.0) * 10)
        }
    }

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int): Int {
        if (type.silkTouch) {
            if (enchantment == Enchantments.SILK_TOUCH) return oldLevel atLeast 1
        }
        return oldLevel
    }

    override fun convertItemStack(itemStack: ItemStack): ItemStack {
        var itemStack2 = itemStack
        if (type.silkTouch) {
            itemStack2 = itemStack2.copy()
            val enchantments = EnchantmentHelper.get(itemStack2)
            enchantments[Enchantments.SILK_TOUCH] = enchantments.getOrElse(Enchantments.SILK_TOUCH) { 0 } atLeast 1
            EnchantmentHelper.set(enchantments, itemStack2)
        }
        return itemStack2
    }
}
