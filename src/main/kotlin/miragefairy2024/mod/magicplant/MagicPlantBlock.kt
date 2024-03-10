package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.createItemStack
import miragefairy2024.util.darkGray
import miragefairy2024.util.darkRed
import miragefairy2024.util.formatted
import miragefairy2024.util.green
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import miragefairy2024.util.toBlockPos
import miragefairy2024.util.toBox
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import mirrg.kotlin.hydrogen.max
import mirrg.kotlin.hydrogen.or
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Fertilizable
import net.minecraft.block.PlantBlock
import net.minecraft.block.SideShapeType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.text.Text
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
abstract class MagicPlantBlock(settings: Settings) : PlantBlock(settings), BlockEntityProvider, Fertilizable {

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
        }
    }


    // Growth

    /** このサイズは成長が可能か。 */
    protected abstract fun canGrow(blockState: BlockState): Boolean

    /** 指定のサイズで成長した後のサイズを返す。 */
    protected abstract fun getBlockStateAfterGrowth(blockState: BlockState, amount: Int): BlockState

    /** 時間経過や骨粉などによって呼び出される成長と自動収穫などのためのイベントを処理します。 */
    protected fun move(world: ServerWorld, blockPos: BlockPos, blockState: BlockState, speed: Double = 1.0, autoPick: Boolean = false) {
        val traitStacks = world.getTraitStacks(blockPos) ?: return
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
            pick(world, blockPos, null, null)
        }

    }

    final override fun hasRandomTicks(state: BlockState) = true
    final override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) = move(world, pos, state, autoPick = true)

    final override fun isFertilizable(world: WorldView, pos: BlockPos, state: BlockState) = canGrow(state)
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
    protected fun createSeed(traitStacks: TraitStacks): ItemStack {
        val itemStack = this.asItem().createItemStack()
        itemStack.setTraitStacks(traitStacks)
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
            val targetTraitStacks = world.getTraitStacks(targetBlockPos) ?: return
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
    protected fun pick(world: ServerWorld, blockPos: BlockPos, player: PlayerEntity?, tool: ItemStack?) {

        // ドロップアイテムを計算
        val blockState = world.getBlockState(blockPos)
        val block = blockState.block
        val traitStacks = world.getTraitStacks(blockPos) ?: return
        val traitEffects = calculateTraitEffects(world, blockPos, traitStacks)
        val drops = getAdditionalDrops(world, blockPos, block, blockState, traitStacks, traitEffects, player, tool)
        val experience = world.random.randomInt(traitEffects[TraitEffectKeyCard.EXPERIENCE_PRODUCTION.traitEffectKey])

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
        pick(world as ServerWorld, pos, player, player.mainHandStack)
        return ActionResult.CONSUME
    }

    /** 中央クリックをした際は、この植物の本来の種子を返す。 */
    final override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        val traitStacks = world.getTraitStacks(pos) ?: return EMPTY_ITEM_STACK
        return createSeed(traitStacks)
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

            itemStacks += createSeed(traitStacks)
            itemStacks += getAdditionalDrops(world, blockPos, block, blockState, traitStacks, traitEffects, player, tool)
        }
        return itemStacks
    }

    /** 破壊時、経験値をドロップする。 */
    // 経験値のドロップを onStacksDropped で行うと BlockEntity が得られないためこちらで実装する
    final override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) run {
            if (world !is ServerWorld) return@run
            val traitStacks = world.getTraitStacks(pos) ?: return@run
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

abstract class SimpleMagicPlantBlock(settings: Settings) : MagicPlantBlock(settings) {

    // Property

    abstract val ageProperty: IntProperty

    @Suppress("LeakingThis") // 親クラスのコンストラクタでappendPropertiesが呼ばれるため回避不可能
    val maxAge: Int = ageProperty.values.max()

    init {
        @Suppress("LeakingThis") // 親クラスのコンストラクタでappendPropertiesが呼ばれるため回避不可能
        defaultState = defaultState.with(ageProperty, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(ageProperty)
    }

    fun getAge(state: BlockState) = state[ageProperty]!!
    fun isMaxAge(state: BlockState) = getAge(state) >= maxAge
    fun withAge(age: Int): BlockState = defaultState.with(ageProperty, age atLeast 0 atMost maxAge)


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
            if (fruitCount > 0) drops += getFruitDrops(fruitCount)
        }

        if (isMaxAge(blockState)) {
            val leafCount = world.random.randomInt(leafGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (leafCount > 0) drops += getLeafDrops(leafCount)
        }

        return drops
    }

    abstract fun getFruitDrops(count: Int): List<ItemStack>
    abstract fun getLeafDrops(count: Int): List<ItemStack>

}

abstract class MagicPlantBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state) {

    private var traitStacks: TraitStacks? = null

    fun getTraitStacks() = traitStacks

    fun setTraitStacks(traitStacks: TraitStacks) {
        this.traitStacks = traitStacks
        markDirty()
    }

    override fun setWorld(world: World) {
        super.setWorld(world)
        if (traitStacks == null) {
            val block = world.getBlockState(pos).block
            val traitStackList = mutableListOf<TraitStack>()
            worldGenTraitGenerations.forEach {
                traitStackList += it.spawn(world, pos, block)
            }
            setTraitStacks(TraitStacks.of(traitStackList))
        }
    }

    public override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        traitStacks = TraitStacks.readFromNbt(nbt)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val nbt = super.toInitialChunkDataNbt()
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        return nbt
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)

}

fun BlockView.getTraitStacks(blockPos: BlockPos): TraitStacks? {
    val blockEntity = this.getBlockEntity(blockPos) as? MagicPlantBlockEntity ?: return null
    return blockEntity.getTraitStacks()
}

class MagicPlantSeedItem(block: Block, settings: Settings) : AliasedBlockItem(block, settings) {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        if (world == null) return
        val player = MirageFairy2024.clientProxy?.getClientPlayer() ?: return

        // 特性を得る、無い場合はクリエイティブ専用
        val traitStacks = stack.getTraitStacks() ?: run {
            tooltip += text { CREATIVE_ONLY_TRANSLATION().yellow }
            return
        }

        // プレイヤーのメインハンドの種子の特性を得る
        val otherTraitStacks = if (player.mainHandStack.item == this) player.mainHandStack.getTraitStacks() else null

        // ヘッダー行
        run {
            val countText = when {
                otherTraitStacks == null -> text { "${traitStacks.traitStackList.size}"() }
                traitStacks.traitStackList.size > otherTraitStacks.traitStackList.size -> text { "${traitStacks.traitStackList.size}"().green }
                traitStacks.traitStackList.size == otherTraitStacks.traitStackList.size -> text { "${traitStacks.traitStackList.size}"().darkGray }
                else -> text { "${traitStacks.traitStackList.size}"().darkRed }
            }
            val bitCountText = when {
                otherTraitStacks == null -> text { "${traitStacks.bitCount}"() }
                traitStacks.bitCount > otherTraitStacks.bitCount -> text { "${traitStacks.bitCount}"().green }
                traitStacks.bitCount == otherTraitStacks.bitCount -> text { "${traitStacks.bitCount}"().darkGray }
                else -> text { "${traitStacks.bitCount}"().darkRed }
            }
            tooltip += text { TRAIT_TRANSLATION() + ": x"() + countText + " ("() + bitCountText + "b)"() }
        }

        // 特性行
        traitStacks.traitStackMap.entries
            .sortedBy { it.key }
            .forEach { (trait, level) ->
                val levelText = when {
                    otherTraitStacks == null -> text { level.toString(2)() }

                    else -> {
                        val otherLevel = otherTraitStacks.traitStackMap[trait] ?: 0
                        val bits = (level max otherLevel).toString(2).length
                        (bits - 1 downTo 0).map { bit ->
                            val mask = 1 shl bit
                            val possession = if (level and mask != 0) 1 else 0
                            val otherPossession = if (otherLevel and mask != 0) 1 else 0
                            when {
                                possession > otherPossession -> text { "$possession"().green }
                                possession == otherPossession -> text { "$possession"().darkGray }
                                else -> text { "$possession"().darkRed }
                            }
                        }.join()
                    }
                }

                val traitEffects = trait.getTraitEffects(world, player.blockPos, level)
                tooltip += if (traitEffects != null) {
                    val description = text {
                        traitEffects.effects
                            .map { it.getDescription() }
                            .reduce { a, b -> a + ","() + b }
                    }
                    text { ("  "() + trait.getName() + " "() + levelText + " ("() + description + ")"()).formatted(trait.color) }
                } else {
                    text { ("  "() + trait.getName() + " "() + levelText + " ("() + INVALID_TRANSLATION() + ")"()).darkGray }
                }
            }

    }

    override fun place(context: ItemPlacementContext): ActionResult {
        if (context.stack.getTraitStacks() != null) {
            return super.place(context)
        } else {
            val player = context.player ?: return ActionResult.FAIL
            if (!player.isCreative) return ActionResult.FAIL
            return super.place(context)
        }
    }
}

fun ItemStack.getTraitStacks(): TraitStacks? {
    val nbt = this.nbt ?: return null
    return TraitStacks.readFromNbt(nbt)
}

fun ItemStack.setTraitStacks(traitStacks: TraitStacks) {
    getOrCreateNbt().put("TraitStacks", traitStacks.toNbt())
}
