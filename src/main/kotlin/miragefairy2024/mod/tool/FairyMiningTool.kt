package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.ToolMaterialCard
import miragefairy2024.mod.translation
import miragefairy2024.util.NeighborType
import miragefairy2024.util.Translation
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.registerItemTagGeneration
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.fabricmc.yarn.constants.MiningLevels
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
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
    var mineAll = false
    var cutAll = false
    val descriptions = mutableListOf<Translation>()

    override fun createItem() = FairyMiningToolItem(this, Item.Settings())

    override fun init(card: ToolCard<FairyMiningToolItem>) {
        tags.forEach {
            card.item.registerItemTagGeneration { it }
        }
        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }

    override fun addPoems(poemList: PoemList) = descriptions.fold(poemList) { it, description -> it.translation(PoemType.DESCRIPTION, description) }

}

fun FairyMiningToolType.mineAll() = this.also {
    it.mineAll = true
    it.descriptions += FairyMiningToolItem.MINE_ALL_TRANSLATION
}

fun FairyMiningToolType.cutAll() = this.also {
    it.cutAll = true
    it.descriptions += FairyMiningToolItem.CUT_ALL_TRANSLATION
}

// Sword 3, -2.4

// Shovel 1.5, -3.0

fun FairyMiningToolType.pickaxe(): FairyMiningToolType {
    return this.also {
        it.attackDamage = 1F
        it.attackSpeed = -2.8F
        it.tags += ItemTags.PICKAXES
        it.tags += ItemTags.CLUSTER_MAX_HARVESTABLES
        it.effectiveBlockTags += BlockTags.PICKAXE_MINEABLE
    }
}

/**
 * @param attackDamage wood: 6.0, stone: 7.0, gold: 6.0, iron: 6.0, diamond: 5.0, netherite: 5.0
 * @param attackSpeed wood: -3.2, stone: -3.2, gold: -3.0, iron: -3.1, diamond: -3.0, netherite: -3.0
 */
fun FairyMiningToolType.axe(attackDamage: Float, attackSpeed: Float): FairyMiningToolType {
    return this.also {
        it.attackDamage = attackDamage
        it.attackSpeed = attackSpeed
        it.tags += ItemTags.AXES
        it.effectiveBlockTags += BlockTags.AXE_MINEABLE
    }
}

// Hoe
// @param attackDamage wood: 0.0, stone: -1.0, gold: 0.0, iron: -2.0, diamond: -3.0, netherite: -4.0
// @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0


class FairyMiningToolItem(private val type: FairyMiningToolType, settings: Settings) : MiningToolItem(type.attackDamage, type.attackSpeed, type.toolMaterialCard.toolMaterial, BlockTags.PICKAXE_MINEABLE, settings) {
    companion object {
        val MINE_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.modId}.fairy_mining_tool.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")
        val CUT_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.modId}.fairy_mining_tool.cut_all" }, "Cut down the entire tree", "木全体を伐採")
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
        if (type.mineAll && !miner.isSneaking) run fail@{
            if (world.isClient) return@fail

            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
            if (!state.isIn(ConventionalBlockTags.ORES)) return@fail // 掘ったブロックが鉱石ではない

            // 発動

            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 31) { _, toBlockPos ->
                world.getBlockState(toBlockPos).block === state.block
            }.forEach { (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    stack.damage(1, miner) {
                        it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                    }
                }
            }
        }
        if (type.cutAll && !miner.isSneaking) run fail@{
            if (world.isClient) return@fail

            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
            if (!state.isIn(BlockTags.LOGS)) return@fail // 掘ったブロックが原木ではない

            // 発動

            val logBlockPosList = mutableListOf<BlockPos>()
            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 19, neighborType = NeighborType.VERTICES) { _, toBlockPos ->
                world.getBlockState(toBlockPos).isIn(BlockTags.LOGS)
            }.forEach { (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    stack.damage(1, miner) {
                        it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                    }
                    logBlockPosList += blockPos
                }
            }
            blockVisitor(logBlockPosList, visitOrigins = false, maxDistance = 8) { _, toBlockPos ->
                world.getBlockState(toBlockPos).isIn(BlockTags.LEAVES)
            }.forEach { (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (miner.random.nextFloat() < 0.1F) {
                        stack.damage(1, miner) {
                            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                        }
                    }
                }
            }
        }
        return true
    }
}
