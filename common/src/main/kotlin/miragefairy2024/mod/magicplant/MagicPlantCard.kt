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

    abstract fun getAgeProperty(): IntegerProperty
    abstract fun createBlock(): B

    abstract val family: ResourceLocation
    abstract val possibleTraits: Set<Trait>

    open val baseGrowth = 1.0
    abstract val drops: List<() -> Item>

    open fun createAdvancement(identifier: ResourceLocation): AdvancementCard? = null

    context(ModContext)
    open fun init() {

        // 登録
        card.block.register()
        card.blockEntityType.register()
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
        card.item.registerPoem(seedPoemList)
        card.item.registerPoemGeneration(seedPoemList)

        // 性質
        //card.block.registerTagGenerate(BlockTags.SMALL_FLOWERS) // これをやるとエンダーマンが勝手に引っこ抜いていく
        card.block.registerBlockTagGeneration { BlockTags.MAINTAINS_FARMLAND }
        card.block.registerBlockTagGeneration { BlockTags.SWORD_EFFICIENT }

        // レシピ
        card.item.registerComposterInput(0.3F) // 種はコンポスターに投入可能
        card.item.registerHarvestNotation(drops)

        // 進捗
        card.advancement?.init()

    }
}

open class MagicPlantCard<B : MagicPlantBlock>(private val configuration: MagicPlantConfiguration<*, B>) {
    val blockIdentifier = MirageFairy2024.identifier(configuration.blockPath)
    val itemIdentifier = MirageFairy2024.identifier(configuration.itemPath)
    val block = Registration(BuiltInRegistries.BLOCK, blockIdentifier) { configuration.createBlock() }
    private fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = MagicPlantBlockEntity(configuration, blockPos, blockState)
    val blockEntityType = Registration(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockIdentifier) { BlockEntityType(::createBlockEntity, setOf(block.await()), null) }
    val item = Registration(BuiltInRegistries.ITEM, itemIdentifier) { MagicPlantSeedItem(block.await(), Item.Properties()) }
    val possibleTraits = configuration.possibleTraits
    val advancement = configuration.createAdvancement(blockIdentifier)

    context(ModContext)
    fun init() = configuration.init()
}
