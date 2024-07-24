package miragefairy2024.mod.fairyhouse

import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.BlockPos

object FairyHouseCard : AbstractFairyHouseCard<FairyHouseBlock, FairyHouseBlockEntity>(
    "fairy_house", 2, "Fairy House", "妖精の家",
    "TODO", "TODO", // TODO
    { FairyHouseBlock(it) },
    { pos, state -> FairyHouseBlockEntity(pos, state) },
) {
    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(item) {
            pattern("#U#")
            pattern("L*R")
            pattern("#D#")
            input('#', item)
            input('U', Items.LANTERN)
            input('D', ItemTags.WOOL_CARPETS)
            input('L', ConventionalItemTags.GLASS_PANES)
            input('R', ItemTags.WOODEN_DOORS)
            input('*', MaterialCard.FAIRY_CRYSTAL.item)
        } on MaterialCard.FAIRY_CRYSTAL.item
    }
}

class FairyHouseBlock(settings: Settings) : AbstractFairyHouseBlock(settings) {
    override fun createBlockEntity(pos: BlockPos, state: BlockState) = FairyHouseBlockEntity(pos, state)
}

class FairyHouseBlockEntity(pos: BlockPos, state: BlockState) : AbstractFairyHouseBlockEntity(FairyHouseCard.blockEntityType, pos, state)
