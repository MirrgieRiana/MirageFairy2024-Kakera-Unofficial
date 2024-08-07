package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

open class MagicPlantCard<B : MagicPlantBlock>(settings: MagicPlantSettings<*, B>) {
    val blockIdentifier = Identifier(MirageFairy2024.modId, settings.blockPath)
    val itemIdentifier = Identifier(MirageFairy2024.modId, settings.itemPath)
    val block = settings.createBlock()
    private fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = MagicPlantBlockEntity(blockEntityType, blockPos, blockState)
    val blockEntityType: BlockEntityType<MagicPlantBlockEntity> = BlockEntityType(::createBlockEntity, setOf(block), null)
    val item = MagicPlantSeedItem(block, Item.Settings())
}
