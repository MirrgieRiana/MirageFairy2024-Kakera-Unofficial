package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MagicPlantBlock
import miragefairy2024.mod.magicplant.MagicPlantCard
import miragefairy2024.mod.magicplant.MagicPlantConfiguration
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.TraitStacks
import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.randomInt
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.data.client.Models
import net.minecraft.data.client.TextureKey
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

abstract class SimpleMagicPlantConfiguration<C : SimpleMagicPlantCard<B>, B : SimpleMagicPlantBlock> : MagicPlantConfiguration<C, B>() {
    abstract val outlineShapes: List<VoxelShape>

    open val baseSeedGeneration = 1.0
    open val baseFruitGeneration = 1.0
    open val baseLeafGeneration = 1.0
    open val baseRareGeneration = 1.0

    open fun getFruitDrops(count: Int, random: Random): List<ItemStack> = listOf()
    open fun getLeafDrops(count: Int, random: Random): List<ItemStack> = listOf()
    open fun getRareDrops(count: Int, random: Random): List<ItemStack> = listOf()

    context(ModContext)
    override fun init() {
        super.init()

        // 見た目
        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()) with card.block.getAgeProperty() }
        card.block.getAgeProperty().values.forEach { age ->
            registerModelGeneration({ "block/" * card.block.getIdentifier() * "_age$age" }) {
                Models.CROSS.with(TextureKey.CROSS to "block/magic_plant/" * card.block.getIdentifier() * "_age$age")
            }
        }

    }
}

abstract class SimpleMagicPlantCard<B : SimpleMagicPlantBlock>(configuration: SimpleMagicPlantConfiguration<*, B>) : MagicPlantCard<B>(configuration)

abstract class SimpleMagicPlantBlock(private val configuration: SimpleMagicPlantConfiguration<*, *>, settings: Settings) : MagicPlantBlock(configuration, settings) {

    // Property

    abstract fun getAgeProperty(): IntProperty

    @Suppress("LeakingThis") // 親クラスのコンストラクタでappendPropertiesが呼ばれるため回避不可能
    private val agePropertyCache = getAgeProperty()

    val maxAge: Int = agePropertyCache.values.max()

    init {
        defaultState = defaultState.with(agePropertyCache, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(getAgeProperty()/* この関数は親クラスのinitで呼ばれるのでフィールドを参照できない */)
    }

    fun getAge(state: BlockState) = state[agePropertyCache]!!
    fun isMaxAge(state: BlockState) = getAge(state) >= maxAge
    fun withAge(age: Int): BlockState = defaultState.with(agePropertyCache, age atLeast 0 atMost maxAge)


    // Shape

    private val outlineShapesCache = configuration.outlineShapes.toTypedArray()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = outlineShapesCache[getAge(state)]


    // Magic Plant

    override fun canCross(world: World, blockPos: BlockPos, blockState: BlockState) = isMaxAge(blockState)
    override fun canGrow(blockState: BlockState) = !isMaxAge(blockState)
    override fun getBlockStateAfterGrowth(blockState: BlockState, amount: Int) = withAge(getAge(blockState) + amount atMost maxAge)
    override fun canPick(blockState: BlockState) = isMaxAge(blockState)
    override fun getBlockStateAfterPicking(blockState: BlockState) = withAge(0)

    override fun getAdditionalDrops(world: World, blockPos: BlockPos, block: Block, blockState: BlockState, traitStacks: TraitStacks, traitEffects: MutableTraitEffects, player: PlayerEntity?, tool: ItemStack?): List<ItemStack> {
        val drops = mutableListOf<ItemStack>()

        val fortune = if (tool != null) EnchantmentHelper.getLevel(Enchantments.FORTUNE, tool).toDouble() else 0.0
        val luck = player?.getAttributeValue(EntityAttributes.GENERIC_LUCK) ?: 0.0

        val seedGeneration = traitEffects[TraitEffectKeyCard.SEEDS_PRODUCTION.traitEffectKey]
        val fruitGeneration = traitEffects[TraitEffectKeyCard.FRUITS_PRODUCTION.traitEffectKey]
        val leafGeneration = traitEffects[TraitEffectKeyCard.LEAVES_PRODUCTION.traitEffectKey]
        val rareGeneration = traitEffects[TraitEffectKeyCard.RARE_PRODUCTION.traitEffectKey]
        val generationBoost = traitEffects[TraitEffectKeyCard.PRODUCTION_BOOST.traitEffectKey]
        val fortuneFactor = traitEffects[TraitEffectKeyCard.FORTUNE_FACTOR.traitEffectKey]
        val crossbreeding = traitEffects[TraitEffectKeyCard.CROSSBREEDING.traitEffectKey]

        if (isMaxAge(blockState)) {
            val seedCount = world.random.randomInt(configuration.baseSeedGeneration * seedGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            repeat(seedCount) {
                drops += calculateCrossedSeed(world, blockPos, traitStacks, crossbreeding)
            }
        }

        if (isMaxAge(blockState)) {
            val fruitCount = world.random.randomInt(configuration.baseFruitGeneration * fruitGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (fruitCount > 0) drops += configuration.getFruitDrops(fruitCount, world.random)
        }

        if (isMaxAge(blockState)) {
            val leafCount = world.random.randomInt(configuration.baseLeafGeneration * leafGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (leafCount > 0) drops += configuration.getLeafDrops(leafCount, world.random)
        }

        if (isMaxAge(blockState)) {
            val rareCount = world.random.randomInt(configuration.baseRareGeneration * rareGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (rareCount > 0) drops += configuration.getRareDrops(rareCount, world.random)
        }

        return drops
    }

}
