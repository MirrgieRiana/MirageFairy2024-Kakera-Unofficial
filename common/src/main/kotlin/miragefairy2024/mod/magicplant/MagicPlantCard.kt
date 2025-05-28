package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.PushReaction as PistonBehavior

@Suppress("LeakingThis") // ブートストラップ問題のため解決不可能なので妥協する
abstract class MagicPlantCard<B : MagicPlantBlock> {
    companion object {
        fun createCommonSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)
    }

    abstract fun getBlockPath(): String
    val blockIdentifier = MirageFairy2024.identifier(getBlockPath())
    abstract val blockName: EnJa
    abstract val ageProperty: IntegerProperty
    abstract fun createBlock(): B
    val block = Registration(BuiltInRegistries.BLOCK, blockIdentifier) { createBlock() }

    abstract fun getItemPath(): String
    val itemIdentifier = MirageFairy2024.identifier(getItemPath())
    abstract val itemName: EnJa
    val item = Registration(BuiltInRegistries.ITEM, itemIdentifier) { MagicPlantSeedItem(block.await(), Item.Properties()) }

    private fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = MagicPlantBlockEntity(this, blockPos, blockState)
    val blockEntityType = Registration(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockIdentifier) { BlockEntityType(::createBlockEntity, setOf(block.await()), null) }

    abstract val tier: Int
    abstract val poem: EnJa
    abstract val classification: EnJa

    abstract val family: ResourceLocation
    abstract val possibleTraits: Set<Trait>

    open val baseGrowth = 1.0
    abstract val drops: List<() -> Item>

    open fun createAdvancement(identifier: ResourceLocation): AdvancementCard? = null
    val advancement = createAdvancement(blockIdentifier)

    context(ModContext)
    open fun init() {

        // 登録
        block.register()
        blockEntityType.register()
        item.register()

        // 分類
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // 見た目
        block.registerCutoutRenderLayer()
        item.registerGeneratedModelGeneration()

        // 翻訳
        block.enJa(blockName)
        item.enJa(itemName)
        val seedPoemList = PoemList(tier)
            .poem(poem)
            .poem("classification", classification)
        item.registerPoem(seedPoemList)
        item.registerPoemGeneration(seedPoemList)

        // 性質
        //block.registerTagGenerate(BlockTags.SMALL_FLOWERS) // これをやるとエンダーマンが勝手に引っこ抜いていく
        block.registerBlockTagGeneration { BlockTags.MAINTAINS_FARMLAND }
        block.registerBlockTagGeneration { BlockTags.SWORD_EFFICIENT }

        // レシピ
        item.registerComposterInput(0.3F) // 種はコンポスターに投入可能
        item.registerHarvestNotation(drops)

        // 進捗
        advancement?.init()

    }
}
