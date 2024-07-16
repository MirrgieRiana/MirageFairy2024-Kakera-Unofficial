package miragefairy2024.mod.tool

import miragefairy2024.mod.ToolMaterialCard
import miragefairy2024.util.registerItemTagGeneration
import net.fabricmc.yarn.constants.MiningLevels
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey

// Sword 3, -2.4

// Shovel 1.5, -3.0

class PickaxeType(
    private val toolMaterialCard: ToolMaterialCard,
) : ToolType<FairyMiningToolItem> {
    override fun createItem() = FairyMiningToolItem(toolMaterialCard.toolMaterial, 1F, -2.8F, listOf(BlockTags.PICKAXE_MINEABLE), Item.Settings())
    override fun init(card: ToolCard<FairyMiningToolItem>) {
        card.item.registerItemTagGeneration { ItemTags.CLUSTER_MAX_HARVESTABLES }
        card.item.registerItemTagGeneration { ItemTags.PICKAXES }

        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }
}

/**
 * @param attackDamage wood: 6.0, stone: 7.0, gold: 6.0, iron: 6.0, diamond: 5.0, netherite: 5.0
 * @param attackSpeed wood: -3.2, stone: -3.2, gold: -3.0, iron: -3.1, diamond: -3.0, netherite: -3.0
 */
class AxeType(
    private val toolMaterialCard: ToolMaterialCard,
    private val attackDamage: Float,
    private val attackSpeed: Float,
) : ToolType<FairyMiningToolItem> {
    override fun createItem() = FairyMiningToolItem(toolMaterialCard.toolMaterial, attackDamage, attackSpeed, listOf(BlockTags.AXE_MINEABLE), Item.Settings())

    override fun init(card: ToolCard<FairyMiningToolItem>) {
        card.item.registerItemTagGeneration { ItemTags.AXES }

        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }
}

// Hoe
// @param attackDamage wood: 0.0, stone: -1.0, gold: 0.0, iron: -2.0, diamond: -3.0, netherite: -4.0
// @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0

class FairyMiningToolItem(
    toolMaterial: ToolMaterial,
    attackDamage: Float,
    attackSpeed: Float,
    private val effectiveBlockTags: List<TagKey<Block>>,
    settings: Settings,
) : MiningToolItem(attackDamage, attackSpeed, toolMaterial, BlockTags.PICKAXE_MINEABLE, settings) {
    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState) = if (effectiveBlockTags.any { state.isIn(it) }) miningSpeed else 1.0F
    override fun isSuitableFor(state: BlockState): Boolean {
        val itemMiningLevel = material.miningLevel
        return when {
            itemMiningLevel < MiningLevels.DIAMOND && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) -> false
            itemMiningLevel < MiningLevels.IRON && state.isIn(BlockTags.NEEDS_IRON_TOOL) -> false
            itemMiningLevel < MiningLevels.STONE && state.isIn(BlockTags.NEEDS_STONE_TOOL) -> false
            else -> effectiveBlockTags.any { state.isIn(it) }
        }
    }
}
