package miragefairy2024.mod.fairyhouse

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.BlockStateVariantRotation
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

context(ModContext)
fun initFairyHouseModule() {
    FairyHouseCard.init()
}


open class AbstractFairyHouseCard<B : AbstractFairyHouseBlock, E : AbstractFairyHouseBlockEntity>(
    path: String,
    private val tier: Int,
    private val enName: String,
    private val jaName: String,
    private val enPoem: String,
    private val jaPoem: String,
    blockCreator: (FabricBlockSettings) -> B,
    blockEntityCreator: (BlockPos, BlockState) -> E,
) {
    val identifier = Identifier(MirageFairy2024.modId, path)
    val block = blockCreator(FabricBlockSettings.create().nonOpaque())
    val blockEntityType = BlockEntityType(blockEntityCreator, setOf(block), null)
    val item = BlockItem(block, Item.Settings())

    context(ModContext)
    open fun init() {

        block.register(Registries.BLOCK, identifier)
        blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, identifier)
        item.register(Registries.ITEM, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        block.registerVariantsBlockStateGeneration {
            val normal = BlockStateVariant(model = "block/" * block.getIdentifier())
            listOf(
                propertiesOf(HorizontalFacingBlock.FACING with Direction.NORTH) to normal.with(y = BlockStateVariantRotation.R0),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.EAST) to normal.with(y = BlockStateVariantRotation.R90),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.SOUTH) to normal.with(y = BlockStateVariantRotation.R180),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.WEST) to normal.with(y = BlockStateVariantRotation.R270),
            )
        }
        block.registerCutoutRenderLayer()

        block.enJa(enName, jaName)
        val poemList = PoemList(tier).poem(enPoem, jaPoem)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }

        block.registerDefaultLootTableGeneration()

    }
}

abstract class AbstractFairyHouseBlock(settings: Settings) : SimpleHorizontalFacingBlock(settings), BlockEntityProvider

abstract class AbstractFairyHouseBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state)
