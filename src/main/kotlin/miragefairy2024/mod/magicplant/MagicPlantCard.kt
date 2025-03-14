package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos

open class MagicPlantCard<B : MagicPlantBlock>(private val configuration: MagicPlantConfiguration<*, B>) {
    val blockIdentifier = MirageFairy2024.identifier(configuration.blockPath)
    val itemIdentifier = MirageFairy2024.identifier(configuration.itemPath)
    val block = configuration.createBlock()
    private fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = MagicPlantBlockEntity(configuration, blockPos, blockState)
    val blockEntityType: BlockEntityType<MagicPlantBlockEntity> = BlockEntityType(::createBlockEntity, setOf(block), null)
    val item = MagicPlantSeedItem(block, Item.Settings())
    val possibleTraits = configuration.possibleTraits

    context(ModContext)
    fun init() = configuration.init()
}
