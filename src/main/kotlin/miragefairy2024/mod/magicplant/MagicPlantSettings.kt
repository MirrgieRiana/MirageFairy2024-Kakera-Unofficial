package miragefairy2024.mod.magicplant

import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
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
import net.minecraft.util.Identifier

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

    abstract val family: Identifier
    abstract val possibleTraits: Set<Trait>

    open val baseGrowth = 1.0
    abstract val drops: List<Item>

    context(ModContext)
    open fun init() {

        // 登録
        card.block.register(Registries.BLOCK, card.blockIdentifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.blockIdentifier)
        card.item.register(Registries.ITEM, card.itemIdentifier)

        // 分類
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // 見た目
        card.block.registerCutoutRenderLayer()
        card.item.registerGeneratedModelGeneration()

        // 翻訳
        card.block.enJa(EnJa(blockEnName, blockJaName))
        card.item.enJa(EnJa(itemEnName, itemJaName))
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
