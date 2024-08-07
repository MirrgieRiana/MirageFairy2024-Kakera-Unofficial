package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.mod.magicplant.MagicPlantBlock
import miragefairy2024.mod.magicplant.MagicPlantCard
import miragefairy2024.mod.magicplant.MagicPlantSettings
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.TraitStacks
import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.util.randomInt
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
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

abstract class SimpleMagicPlantSettings<C : SimpleMagicPlantCard<B>, B : SimpleMagicPlantBlock> : MagicPlantSettings<C, B>() {
    abstract val outlineShapes: Array<VoxelShape>
    open fun getFruitDrops(count: Int, random: Random): List<ItemStack> = listOf()
    open fun getLeafDrops(count: Int, random: Random): List<ItemStack> = listOf()
    open fun getRareDrops(count: Int, random: Random): List<ItemStack> = listOf()
}

abstract class SimpleMagicPlantCard<B : SimpleMagicPlantBlock>(settings: SimpleMagicPlantSettings<*, B>) : MagicPlantCard<B>(settings)

abstract class SimpleMagicPlantBlock(private val magicPlantSettings: SimpleMagicPlantSettings<*, *>, settings: Settings) : MagicPlantBlock(magicPlantSettings, settings) {

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

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = magicPlantSettings.outlineShapes[getAge(state)]


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

        if (isMaxAge(blockState)) {
            val seedCount = world.random.randomInt(seedGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            repeat(seedCount) {
                drops += calculateCrossedSeed(world, blockPos, traitStacks)
            }
        }

        if (isMaxAge(blockState)) {
            val fruitCount = world.random.randomInt(fruitGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (fruitCount > 0) drops += magicPlantSettings.getFruitDrops(fruitCount, world.random)
        }

        if (isMaxAge(blockState)) {
            val leafCount = world.random.randomInt(leafGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (leafCount > 0) drops += magicPlantSettings.getLeafDrops(leafCount, world.random)
        }

        if (isMaxAge(blockState)) {
            val rareCount = world.random.randomInt(0.03 * rareGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (rareCount > 0) drops += magicPlantSettings.getRareDrops(rareCount, world.random)
        }

        return drops
    }

}
