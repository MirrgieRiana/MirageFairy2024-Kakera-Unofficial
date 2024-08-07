package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
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
    open fun init() = settings.run {
        val card = this@MagicPlantCard

        // 登録
        card.block.register(Registries.BLOCK, card.blockIdentifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.blockIdentifier)
        card.item.register(Registries.ITEM, card.itemIdentifier)

        // 分類
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        card.item.registerItemGroup(magicPlantSeedItemGroupCard.itemGroupKey) {
            traitRegistry.entrySet.sortedBy { it.key.value }.flatMap { (_, trait) ->
                (0..3).map { b ->
                    card.item.createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1 shl b))) }
                }
            }
        }

        // 見た目
        card.block.registerCutoutRenderLayer()
        card.item.registerGeneratedModelGeneration()

        // 翻訳
        card.block.enJa(blockEnName, blockJaName)
        card.item.enJa(itemEnName, itemJaName)
        val seedPoemList = PoemList(tier)
            .poem(enPoem, jaPoem)
            .poem("classification", enClassification, jaClassification)
        card.item.registerPoem(seedPoemList)
        card.item.registerPoemGeneration(seedPoemList)

        // 性質
        //card.block.registerTagGenerate(BlockTags.SMALL_FLOWERS) // これをやるとエンダーマンが勝手に引っこ抜いていく
        card.block.registerBlockTagGeneration { BlockTags.MAINTAINS_FARMLAND }

        // レシピ
        card.item.registerComposterInput(0.3F) // 種はコンポスターに投入可能

    }

}
