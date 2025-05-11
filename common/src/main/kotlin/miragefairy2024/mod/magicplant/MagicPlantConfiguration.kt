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
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Item
import net.minecraft.world.level.material.PushReaction as PistonBehavior

abstract class MagicPlantConfiguration<C : MagicPlantCard<B>, B : MagicPlantBlock> {
    companion object {
        fun createCommonSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)
    }

    abstract val card: C

    abstract val blockPath: String
    abstract val blockName: EnJa
    abstract val itemPath: String
    abstract val itemName: EnJa
    abstract val tier: Int
    abstract val poem: EnJa
    abstract val classification: EnJa

    abstract fun createBlock(): B

    abstract val family: ResourceLocation
    abstract val possibleTraits: Set<Trait>

    open val baseGrowth = 1.0
    abstract val drops: List<Item>

    context(ModContext)
    open fun init() {

        // 登録
        BuiltInRegistries.BLOCK.register(card.blockIdentifier) { card.block }
        BuiltInRegistries.BLOCK_ENTITY_TYPE.register(card.blockIdentifier) { card.blockEntityType }
        card.item.register()

        // 分類
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // 見た目
        card.block.registerCutoutRenderLayer()
        card.item.registerGeneratedModelGeneration()

        // 翻訳
        card.block.enJa(blockName)
        card.item.enJa(itemName)
        val seedPoemList = PoemList(tier)
            .poem(poem)
            .poem("classification", classification)
        card.item().registerPoem(seedPoemList)
        card.item().registerPoemGeneration(seedPoemList)

        // 性質
        //card.block.registerTagGenerate(BlockTags.SMALL_FLOWERS) // これをやるとエンダーマンが勝手に引っこ抜いていく
        card.block.registerBlockTagGeneration { BlockTags.MAINTAINS_FARMLAND }
        card.block.registerBlockTagGeneration { BlockTags.SWORD_EFFICIENT }

        // レシピ
        card.item().registerComposterInput(0.3F) // 種はコンポスターに投入可能
        card.item().registerHarvestNotation(drops)

    }
}
