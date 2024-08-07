package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

abstract class MagicPlantCard<S : MagicPlantSettings, B : MagicPlantBlock>(
    val settings: S,
    blockPath: String,
    val blockEnName: String,
    val blockJaName: String,
    itemPath: String,
    val itemEnName: String,
    val itemJaName: String,
    val seedPoemList: PoemList,
    blockCreator: () -> B,
    blockEntityCreator: (BlockPos, BlockState) -> MagicPlantBlockEntity,
) {
    companion object {
        fun createCommonSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)
    }

    val blockIdentifier = Identifier(MirageFairy2024.modId, blockPath)
    val itemIdentifier = Identifier(MirageFairy2024.modId, itemPath)
    val block = blockCreator()
    val blockEntityType = BlockEntityType(blockEntityCreator, setOf(block), null)
    val item = MagicPlantSeedItem(block, Item.Settings())

    context(ModContext)
    open fun init() {

        // 登録
        block.register(Registries.BLOCK, blockIdentifier)
        blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, blockIdentifier)
        item.register(Registries.ITEM, itemIdentifier)

        // 分類
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        item.registerItemGroup(magicPlantSeedItemGroupCard.itemGroupKey) {
            traitRegistry.entrySet.sortedBy { it.key.value }.flatMap { (_, trait) ->
                (0..3).map { b ->
                    item.createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1 shl b))) }
                }
            }
        }

        // 見た目
        block.registerCutoutRenderLayer()
        item.registerGeneratedModelGeneration()

        // 翻訳
        block.enJa(blockEnName, blockJaName)
        item.enJa(itemEnName, itemJaName)
        item.registerPoem(seedPoemList)
        item.registerPoemGeneration(seedPoemList)

        // 性質
        //block.registerTagGenerate(BlockTags.SMALL_FLOWERS) // これをやるとエンダーマンが勝手に引っこ抜いていく
        block.registerBlockTagGeneration { BlockTags.MAINTAINS_FARMLAND }

        // レシピ
        item.registerComposterInput(0.3F) // 種はコンポスターに投入可能

    }

}
