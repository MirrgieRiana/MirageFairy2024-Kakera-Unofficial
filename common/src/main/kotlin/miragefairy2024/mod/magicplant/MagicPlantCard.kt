package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.CompletableRegistration
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

open class MagicPlantCard<B : MagicPlantBlock>(private val configuration: MagicPlantConfiguration<*, B>) {
    val blockIdentifier = MirageFairy2024.identifier(configuration.blockPath)
    val itemIdentifier = MirageFairy2024.identifier(configuration.itemPath)
    val block = CompletableRegistration(BuiltInRegistries.BLOCK, blockIdentifier) { configuration.createBlock() }
    private fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = MagicPlantBlockEntity(configuration, blockPos, blockState)
    val blockEntityType = CompletableRegistration(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockIdentifier) { BlockEntityType(::createBlockEntity, setOf(block.await()), null) }
    val item = CompletableRegistration(BuiltInRegistries.ITEM, itemIdentifier) { MagicPlantSeedItem(block.await(), Item.Properties()) }
    val possibleTraits = configuration.possibleTraits

    context(ModContext)
    fun init() = configuration.init()
}
