package miragefairy2024.mod.magicplant

import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.createItemStack
import miragefairy2024.util.randomInt
import miragefairy2024.util.toBlockPos
import miragefairy2024.util.toBox
import mirrg.kotlin.hydrogen.or
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Fertilizable
import net.minecraft.block.PlantBlock
import net.minecraft.block.SideShapeType
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldView

@Suppress("OVERRIDE_DEPRECATION")
abstract class MagicPlantBlock(private val magicPlantSettings: MagicPlantSettings<*, *>, settings: Settings) : PlantBlock(settings), BlockEntityProvider, Fertilizable {

    // Block Entity

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = MagicPlantBlockEntity(magicPlantSettings.card.blockEntityType, pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }


    // Behaviour

    override fun canPlantOnTop(floor: BlockState, world: BlockView, pos: BlockPos) = world.getBlockState(pos).isSideSolid(world, pos, Direction.UP, SideShapeType.CENTER) || floor.isOf(Blocks.FARMLAND)


    // Trait

    /** 隣接する同種の植物が交配種子を生産するときに参加できるか否か */
    protected abstract fun canCross(world: World, blockPos: BlockPos, blockState: BlockState): Boolean

    /** あるワールド上の地点における特性の効果を計算する。 */
    protected fun calculateTraitEffects(world: World, blockPos: BlockPos, traitStacks: TraitStacks): MutableTraitEffects {
        val allTraitEffects = MutableTraitEffects()
        traitStacks.traitStackMap.forEach { (trait, level) ->
            val traitEffects = trait.getTraitEffects(world, blockPos, level)
            if (traitEffects != null) allTraitEffects += traitEffects
        }
        return allTraitEffects
    }

    /** 種子によって置かれた際にその特性をコピーする。 */
    final override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, pos, state, placer, itemStack)
        run {
            if (world.isClient) return@run
            val blockEntity = world.getBlockEntity(pos) as? MagicPlantBlockEntity ?: return@run
            val traitStacks = itemStack.getTraitStacks() ?: return@run
            blockEntity.setTraitStacks(traitStacks)
            blockEntity.setRare(itemStack.isRare())
        }
    }


    // Growth

    /** このサイズは成長が可能か。 */
    protected abstract fun canGrow(blockState: BlockState): Boolean

    /** 指定のサイズで成長した後のサイズを返す。 */
    protected abstract fun getBlockStateAfterGrowth(blockState: BlockState, amount: Int): BlockState

    /** 時間経過や骨粉などによって呼び出される成長と自動収穫などのためのイベントを処理します。 */
    protected fun move(world: ServerWorld, blockPos: BlockPos, blockState: BlockState, speed: Double = 1.0, autoPick: Boolean = false) {
        val traitStacks = world.getMagicPlantBlockEntity(blockPos)?.getTraitStacks() ?: return
        val traitEffects = calculateTraitEffects(world, blockPos, traitStacks)

        // 成長
        if (canGrow(blockState)) {
            val nutrition = traitEffects[TraitEffectKeyCard.NUTRITION.traitEffectKey]
            val environment = traitEffects[TraitEffectKeyCard.ENVIRONMENT.traitEffectKey]
            val growthBoost = traitEffects[TraitEffectKeyCard.GROWTH_BOOST.traitEffectKey]
            val actualGrowthAmount = world.random.randomInt(0.2 * nutrition * environment * (1 + growthBoost) * speed)
            val newBlockState = getBlockStateAfterGrowth(blockState, actualGrowthAmount)
            if (newBlockState != blockState) {
                world.setBlockState(blockPos, newBlockState, NOTIFY_LISTENERS)
            }
        }

        // 自動収穫
        if (autoPick && canAutoPick(blockState)) run {
            if (world.getEntitiesByType(EntityType.ITEM, blockPos.toBox()) { true }.isNotEmpty()) return@run // アイテムがそこに存在する場合は中止
            if (world.getEntitiesByType(EntityType.EXPERIENCE_ORB, blockPos.toBox()) { true }.isNotEmpty()) return@run // 経験値がそこに存在する場合は中止
            val naturalAbscission = traitEffects[TraitEffectKeyCard.NATURAL_ABSCISSION.traitEffectKey]
            if (!(world.random.nextDouble() < naturalAbscission)) return@run // 確率で失敗
            pick(world, blockPos, null, null, false)
        }

    }

    final override fun hasRandomTicks(state: BlockState) = true
    final override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) = move(world, pos, state, autoPick = true)

    final override fun isFertilizable(world: WorldView, pos: BlockPos, state: BlockState, isClient: Boolean) = canGrow(state)
    final override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState) = true
    final override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) = move(world, pos, state, speed = 10.0)


    // Drop

    /** このサイズは収穫が可能か。 */
    protected abstract fun canPick(blockState: BlockState): Boolean

    /** このサイズは自動収穫が可能か。 */
    protected open fun canAutoPick(blockState: BlockState) = canPick(blockState)

    /** 指定のサイズで収穫した後のサイズを返す。 */
    protected abstract fun getBlockStateAfterPicking(blockState: BlockState): BlockState

    /** 確定で戻って来る本来の種子以外の追加種子及び生産物を計算する。 */
    protected abstract fun getAdditionalDrops(world: World, blockPos: BlockPos, block: Block, blockState: BlockState, traitStacks: TraitStacks, traitEffects: MutableTraitEffects, player: PlayerEntity?, tool: ItemStack?): List<ItemStack>

    /** この植物本来の種子を返す。 */
    protected fun createSeed(traitStacks: TraitStacks, isRare: Boolean = false): ItemStack {
        val itemStack = this.asItem().createItemStack()
        itemStack.setTraitStacks(traitStacks)
        itemStack.setRare(isRare)
        return itemStack
    }

    /** 交配が可能であれば交配された種子、そうでなければこの植物本来の種子を返す。 */
    protected fun calculateCrossedSeed(world: World, blockPos: BlockPos, traitStacks: TraitStacks): ItemStack {

        val targetTraitStacksList = mutableListOf<TraitStacks>()
        fun check(targetBlockPos: BlockPos) {
            val targetBlockState = world.getBlockState(targetBlockPos)
            val targetBlock = targetBlockState.block as? MagicPlantBlock ?: return
            if (targetBlock != this) return
            if (!targetBlock.canCross(world, blockPos, targetBlockState)) return
            val targetTraitStacks = world.getMagicPlantBlockEntity(targetBlockPos)?.getTraitStacks() ?: return
            targetTraitStacksList += targetTraitStacks
        }
        check(blockPos.north())
        check(blockPos.south())
        check(blockPos.west())
        check(blockPos.east())

        if (targetTraitStacksList.isEmpty()) return createSeed(traitStacks)
        val targetTraitStacks = targetTraitStacksList[world.random.nextInt(targetTraitStacksList.size)]

        return createSeed(crossTraitStacks(world.random, traitStacks, targetTraitStacks))
    }

    /** 成長段階を消費して収穫物を得てエフェクトを出す収穫処理。 */
    protected fun pick(world: ServerWorld, blockPos: BlockPos, player: PlayerEntity?, tool: ItemStack?, dropExperience: Boolean) {

        // ドロップアイテムを計算
        val blockState = world.getBlockState(blockPos)
        val block = blockState.block
        val traitStacks = world.getMagicPlantBlockEntity(blockPos)?.getTraitStacks() ?: return
        val traitEffects = calculateTraitEffects(world, blockPos, traitStacks)
        val drops = getAdditionalDrops(world, blockPos, block, blockState, traitStacks, traitEffects, player, tool)
        val experience = if (dropExperience) world.random.randomInt(traitEffects[TraitEffectKeyCard.EXPERIENCE_PRODUCTION.traitEffectKey]) else 0

        // アイテムを生成
        drops.forEach { itemStack ->
            dropStack(world, blockPos, itemStack)
        }
        if (experience > 0) dropExperience(world, blockPos, experience)

        // 成長段階を消費
        world.setBlockState(blockPos, getBlockStateAfterPicking(blockState), NOTIFY_LISTENERS)

        // エフェクト
        world.playSound(null, blockPos, soundGroup.breakSound, SoundCategory.BLOCKS, (soundGroup.volume + 1.0F) / 2.0F * 0.5F, soundGroup.pitch * 0.8F)

    }

    /** 右クリック時、収穫が可能であれば収穫する。 */
    final override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (!canPick(state)) return ActionResult.PASS
        if (world.isClient) return ActionResult.SUCCESS
        pick(world as ServerWorld, pos, player, player.mainHandStack, true)
        return ActionResult.CONSUME
    }

    /** 中央クリックをした際は、この植物の本来の種子を返す。 */
    final override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        val blockEntity = world.getMagicPlantBlockEntity(pos) ?: return EMPTY_ITEM_STACK
        val traitStacks = blockEntity.getTraitStacks() ?: return EMPTY_ITEM_STACK
        return createSeed(traitStacks, isRare = blockEntity.isRare())
    }

    /** 破損時、LootTableと同じところで収穫物を追加する。 */
    // 本来 LootTable を使ってすべて行う想定だが、他にドロップを自由に制御できる場所がないため苦肉の策でここでプログラムで生成する
    final override fun getDroppedStacks(state: BlockState, builder: LootContextParameterSet.Builder): MutableList<ItemStack> {
        val itemStacks = mutableListOf<ItemStack>()
        @Suppress("DEPRECATION")
        itemStacks += super.getDroppedStacks(state, builder)
        run {
            val world = builder.world ?: return@run
            val blockPos = builder.getOptional(LootContextParameters.ORIGIN).or { return@run }.toBlockPos()
            val blockState = builder.getOptional(LootContextParameters.BLOCK_STATE) ?: return@run
            val block = blockState.block
            val blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY) as? MagicPlantBlockEntity ?: return@run
            val traitStacks = blockEntity.getTraitStacks() ?: return@run
            val traitEffects = calculateTraitEffects(world, blockPos, traitStacks)
            val player = builder.getOptional(LootContextParameters.THIS_ENTITY) as? PlayerEntity
            val tool = builder.getOptional(LootContextParameters.TOOL)

            itemStacks += createSeed(traitStacks, isRare = blockEntity.isRare())
            itemStacks += getAdditionalDrops(world, blockPos, block, blockState, traitStacks, traitEffects, player, tool)
        }
        return itemStacks
    }

    /** 破壊時、経験値をドロップする。 */
    // 経験値のドロップを onStacksDropped で行うと BlockEntity が得られないためこちらで実装する
    final override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) run {
            if (world !is ServerWorld) return@run
            if (!canPick(state)) return@run
            val traitStacks = world.getMagicPlantBlockEntity(pos)?.getTraitStacks() ?: return@run
            val traitEffects = calculateTraitEffects(world, pos, traitStacks)
            val experience = world.random.randomInt(traitEffects[TraitEffectKeyCard.EXPERIENCE_PRODUCTION.traitEffectKey])
            if (experience > 0) dropExperience(world, pos, experience)
        }
        @Suppress("DEPRECATION")
        super.onStateReplaced(state, world, pos, newState, moved)
    }


    // Visual

    // TODO パーティクル

}
