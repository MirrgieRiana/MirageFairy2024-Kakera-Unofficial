package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.fairy.CondensedMotifChance
import miragefairy2024.mod.fairy.FairyStatueCard
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.MotifTableScreenHandler
import miragefairy2024.mod.fairy.getIdentifier
import miragefairy2024.mod.fairy.setFairyStatueMotif
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.util.Chance
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.invoke
import miragefairy2024.util.isServer
import miragefairy2024.util.normal
import miragefairy2024.util.obtain
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.totalWeight
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.withHorizontalRotation
import mirrg.kotlin.hydrogen.Single
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.shapes.CollisionContext as ShapeContext
import net.minecraft.world.level.pathfinder.PathComputationType as NavigationType
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.network.FriendlyByteBuf as PacketByteBuf
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.tags.BlockTags
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult as ActionResult
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter as BlockView
import net.minecraft.world.level.Level as World

object FairyStatueFountainCard {
    val identifier = MirageFairy2024.identifier("fairy_statue_fountain")
    val block = FairyStatueFountainBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).strength(1.0F).nonOpaque())
    val item = BlockItem(block, Item.Properties())
}

context(ModContext)
fun initFairyFountainModule() {
    FairyStatueFountainBlock.USAGE_TRANSLATION.enJa()

    FairyStatueFountainCard.let { card ->

        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        card.block.registerCutoutRenderLayer()

        card.block.enJa(EnJa("Fairy Statue Fountain", "妖精の像の泉"))
        val poemList = PoemList(1)
            .poem("Where does this water spring from...?", "この水は一体どこから湧いてくるのだろう…")
            .description("description1", "Can draw lottery with 100 Fairy Jewels", "100フェアリージュエルで抽選ができる")
            .description("description2", "Use while sneaking to show loot table", "スニーク中に使用時、提供割合を表示")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        card.block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_PICKAXE }

        card.block.registerDefaultLootTableGeneration()

    }

    registerShapedRecipeGeneration(FairyStatueFountainCard.item) {
        pattern(" F ")
        pattern("SQS")
        pattern("SSS")
        define('F', MaterialCard.FAIRY_SCALES.item)
        define('Q', MaterialCard.FAIRY_QUEST_CARD_BASE.item)
        define('S', Items.COBBLESTONE)
    } on MaterialCard.FAIRY_SCALES.item
}

class FairyStatueFountainBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val USAGE_TRANSLATION = Translation({ "block.${MirageFairy2024.identifier("fairy_statue_fountain").toLanguageKey()}.usage" }, "Please use it while holding %s", "%sを持って使用してください")
        private val SHAPE: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 9.0, 14.0)
        val recipes = mutableListOf<Recipe>()
    }

    enum class Rarity {
        R, // 85%
        SR, // 9%
        PICKUP_SR, // 3%
        SSR, // 2%
        PICKUP_SSR, // 1%
    }

    class Recipe(val motif: Motif, val rarity: Rarity)


    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType?) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = SHAPE

    @Suppress("OVERRIDE_DEPRECATION")
    override fun use(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (player.isShiftKeyDown) {
            if (world.isClientSide) return ActionResult.SUCCESS

            val chanceTable2 = getChanceTable()
            val chanceTable = chanceTable2.map {
                CondensedMotifChance(
                    showingItemStack = it.item.first?.let { entry -> entry.second.getFairyStatueCard().item.createItemStack().setFairyStatueMotif(entry.first) } ?: Items.IRON_INGOT.createItemStack(),
                    motif = it.item.first?.first ?: MotifCard.AIR,
                    rate = it.weight,
                    count = 1.0,
                )
            }

            player.openMenu(object : ExtendedScreenHandlerFactory {
                override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity) = MotifTableScreenHandler(syncId, chanceTable)
                override fun getDisplayName() = name
                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                    buf.writeInt(chanceTable.size)
                    chanceTable.forEach {
                        buf.writeItem(it.showingItemStack)
                        buf.writeUtf(it.motif.getIdentifier()!!.string)
                        buf.writeDouble(it.rate)
                        buf.writeDouble(it.count)
                    }
                }
            })

            return ActionResult.CONSUME
        } else {
            // 入力判定
            val inputItemStack = player.getItemInHand(hand)
            if (!inputItemStack.`is`(MaterialCard.JEWEL_100.item)) { // 持っているアイテムが違う
                if (world.isServer) player.displayClientMessage(text { USAGE_TRANSLATION(MaterialCard.JEWEL_100.item.description) }, true)
                return ActionResult.CONSUME // なぜかFAILにすると後続のイベントがキャンセルされない
            }
            if (inputItemStack.count < 1) { // 個数が足りない
                if (world.isServer) player.displayClientMessage(text { USAGE_TRANSLATION(MaterialCard.JEWEL_100.item.description) }, true)
                return ActionResult.CONSUME // なぜかFAILにすると後続のイベントがキャンセルされない
            }

            // 成立

            // 消費
            if (!player.isCreative) {
                if (world.isServer) inputItemStack.decrement(1)
            }

            // 生産
            if (world.isServer) {
                val outputItemStack = run {
                    val chanceTable = getChanceTable()
                    val entry = chanceTable.weightedRandom(world.random)?.first
                    entry?.let { it.second.getFairyStatueCard().item.createItemStack().setFairyStatueMotif(it.first) } ?: Items.IRON_INGOT.createItemStack()
                }
                player.obtain(outputItemStack)
            }

            // エフェクト
            if (world.isServer) world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, (world.random.nextFloat() - world.random.nextFloat()) * 0.35F + 0.9F)
            if (world.isClientSide) {
                // TODO サーバーサイドで発火して、全プレイヤーの画面に表示する
                repeat(3) {
                    world.addParticle(
                        ParticleTypeCard.MISSION.particleType,
                        pos.x + 2.0 / 16.0 + world.random.nextDouble() * 12.0 / 16.0,
                        pos.y + 2.0 / 16.0 + world.random.nextDouble() * 4.0 / 16.0,
                        pos.z + 2.0 / 16.0 + world.random.nextDouble() * 12.0 / 16.0,
                        world.random.nextGaussian() * 0.02,
                        world.random.nextGaussian() * 0.02,
                        world.random.nextGaussian() * 0.02,
                    )
                }
            }

            return ActionResult.sidedSuccess(world.isClientSide)
        }
    }

    /** 確率の合計が1.0+εであることが保証されます。 */
    private fun getChanceTable(): List<Chance<Single<Pair<Motif, Rarity>?>>> {
        val chanceTable = mutableListOf<Chance<Single<Pair<Motif, Rarity>?>>>()

        var consumedChance = 0.0

        fun f(threshold: Double, rarity: Rarity) {
            val recipes2 = recipes.filter { it.rarity == rarity }
            if (recipes2.isEmpty()) return
            val chancePerRecipe = (threshold - consumedChance) / recipes2.size.toDouble()
            chanceTable += recipes2.map { Chance(chancePerRecipe, Single(Pair(it.motif, rarity))) }
            consumedChance = threshold
        }
        f(0.01, Rarity.PICKUP_SSR)
        f(0.01 + 0.02, Rarity.SSR)
        f(0.01 + 0.02 + 0.03, Rarity.PICKUP_SR)
        f(0.01 + 0.02 + 0.03 + 0.09, Rarity.SR)
        f(0.01 + 0.02 + 0.03 + 0.09 + 0.85, Rarity.R)

        val totalWeight = chanceTable.totalWeight
        if (totalWeight < 0.9999) chanceTable += Chance(1.0 - totalWeight, Single(null))

        return chanceTable
    }

}

fun FairyStatueFountainBlock.Rarity.getFairyStatueCard() = when (this) {
    FairyStatueFountainBlock.Rarity.R -> FairyStatueCard.FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.SR -> FairyStatueCard.GOLDEN_FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.PICKUP_SR -> FairyStatueCard.GOLDEN_FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.SSR -> FairyStatueCard.FANTASTIC_FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.PICKUP_SSR -> FairyStatueCard.FANTASTIC_FAIRY_STATUE
}
