package miragefairy2024.mod.magicplant

import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerHarvestNotation
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
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags

abstract class MagicPlantSettings<C : MagicPlantCard<B>, B : MagicPlantBlock> {
    companion object {
        fun createCommonSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)
    }

    abstract val card: C

    abstract val blockPath: String
    abstract val blockEnName: String
    abstract val blockJaName: String
    abstract val itemPath: String
    abstract val itemEnName: String
    abstract val itemJaName: String
    abstract val tier: Int
    abstract val enPoem: String
    abstract val jaPoem: String
    abstract val enClassification: String
    abstract val jaClassification: String

    abstract fun createBlock(): B

    abstract val drops: List<Item>

    context(ModContext)
    open fun init() {

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
        card.item.registerHarvestNotation(drops)

    }
}
