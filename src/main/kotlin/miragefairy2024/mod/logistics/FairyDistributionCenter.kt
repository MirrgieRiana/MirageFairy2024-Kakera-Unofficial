package miragefairy2024.mod.logistics

import miragefairy2024.ModContext
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.MapColor
import net.minecraft.item.Items
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.sound.BlockSoundGroup

object FairyDistributionCenterConfiguration : FairyLogisticsBlockConfiguration() {
    override val path = "fairy_distribution_center"
    override val name = EnJa("Fairy Distribution Center", "妖精のお届けもの屋")
    override val tier = 3
    override val poem = EnJa("TODO", "TODO") // TODO
    override fun createBlock() = FairyDistributionCenterBlock(createFairyLogisticsBlockSettings().mapColor(MapColor.PINK).sounds(BlockSoundGroup.WOOD))

    context(ModContext)
    override fun init(card: FairyLogisticsBlockCard) {
        super.init(card)

        card.block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }

        registerShapedRecipeGeneration(card.item) {
            pattern("#A#")
            pattern("DCD")
            pattern("###")
            input('A', BlockMaterialCard.AURA_STONE.item)
            input('#', ItemTags.PLANKS)
            input('C', Items.BARREL)
            input('D', Items.PINK_DYE)
        } on BlockMaterialCard.AURA_STONE.item
    }
}

object FairyDistributionCenterCard : FairyLogisticsBlockCard(FairyDistributionCenterConfiguration)

class FairyDistributionCenterBlock(settings: Settings) : FairyLogisticsBlock(settings) { // TODO

}
