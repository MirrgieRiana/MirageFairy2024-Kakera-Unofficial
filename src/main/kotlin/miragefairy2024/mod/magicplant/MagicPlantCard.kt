package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

abstract class MagicPlantCard<S : MagicPlantSettings<B>, B : MagicPlantBlock>(val settings: S) {
    companion object {
        fun createCommonSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)
    }

    val blockIdentifier = Identifier(MirageFairy2024.modId, settings.blockPath)
    val itemIdentifier = Identifier(MirageFairy2024.modId, settings.itemPath)
    val block = settings.createBlock()
    private fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = MagicPlantBlockEntity(blockEntityType, blockPos, blockState)
    val blockEntityType: BlockEntityType<MagicPlantBlockEntity> = BlockEntityType(::createBlockEntity, setOf(block), null)
    val item = MagicPlantSeedItem(block, Item.Settings())

    context(ModContext)
    fun init() = settings.init(this)
}
