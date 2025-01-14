package miragefairy2024.mod.fairybuilding

import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object FairyHouseCard : FairyFactoryCard<FairyHouseBlock, FairyHouseBlockEntity, FairyHouseScreenHandler>() {
    override fun getPath() = "fairy_house"
    override val tier = 2
    override val name = EnJa("Fairy House", "妖精の家")
    override val poem = EnJa("Home sweet home", "あたたかいおうち")

    override fun createBlock() = FairyHouseBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyHouseBlockEntity)

    override fun createScreenHandler(arguments: FairyBuildingScreenHandler.Arguments) = FairyHouseScreenHandler(this, arguments)

    override val guiWidth = 176
    override val guiHeight = 178

    override fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> {
        return super.createSlotConfigurations() + listOf(
            FairyBuildingSlotConfiguration(8, 39, animation = SlotAnimationConfiguration(false, listOf(Position(10.0, 9.5, 14.5, 0.0F, 180.0F, 200)))), // 本棚の上
            FairyBuildingSlotConfiguration(61, 17, animation = SlotAnimationConfiguration(true, listOf(Position(4.5, 2.2, 14.0, 90.0F, 270.0F, 200)))) { it.isOf(FairyCard.item) }, // ベッド
            FairyBuildingSlotConfiguration(61, 41, animation = SlotAnimationConfiguration(true, listOf(Position(4.5, 7.2, 14.0, 90.0F, 270.0F, 200)))) { it.isOf(FairyCard.item) }, // ベッド
            FairyBuildingSlotConfiguration(61, 65, animation = SlotAnimationConfiguration(true, listOf(Position(4.5, 12.2, 14.0, 90.0F, 270.0F, 200)))) { it.isOf(FairyCard.item) }, // ベッド
            FairyBuildingSlotConfiguration(82, 48, animation = SlotAnimationConfiguration(false, listOf(Position(1.5, 5.5, 2.0, 0.0F, 270.0F, 200)))), // タンスの上
            FairyBuildingSlotConfiguration(104, 27, animation = SlotAnimationConfiguration(true, listOf(Position(12.0, 0.1, 3.0, 0.0F, 280.0F, 200)))) { it.isOf(FairyCard.item) }, // テーブル
            FairyBuildingSlotConfiguration(128, 17, animation = SlotAnimationConfiguration(false, listOf(Position(11.5, 1.5, 7.5, 0.0F, 270.0F, 200)))), // テーブルの上
            FairyBuildingSlotConfiguration(152, 17, animation = SlotAnimationConfiguration(true, listOf(Position(13.0, 0.1, 12.0, 0.0F, 70.0F, 200)))) { it.isOf(FairyCard.item) }, // テーブル
            FairyBuildingSlotConfiguration(152, 37, animation = SlotAnimationConfiguration(true, listOf(Position(9.0, 0.1, 12.0, 0.0F, 110.0F, 200)))) { it.isOf(FairyCard.item) }, // テーブル
            FairyBuildingSlotConfiguration(108, 60, animation = SlotAnimationConfiguration(true, run {
                listOf(
                    Position(2.0, 0.1, 10.0, 0.0F, 110.0F, 140),
                    Position(6.0, 0.1, 10.0, 0.0F, 60.0F, 140),
                )
            })) { it.isOf(FairyCard.item) }, // 床（左下）
            FairyBuildingSlotConfiguration(
                128, 60,
                animation = SlotAnimationConfiguration(false, listOf(Position(4.0, 0.1, 7.0, 0.0F, 90.0F, 200))),
                insertDirections = setOf(Direction.UP, Direction.WEST),
                extractDirections = setOf(Direction.DOWN, Direction.EAST),
            ), // 床の上
            FairyBuildingSlotConfiguration(148, 60, animation = SlotAnimationConfiguration(true, run {
                listOf(
                    Position(5.0, 0.1, 4.0, 0.0F, 290.0F, 180),
                    Position(7.0, 0.1, 6.0, 0.0F, 320.0F, 180),
                )
            })) { it.isOf(FairyCard.item) }, // 床（右上）
        )
    }

    override val collectingFolia = 2_000
    override val maxFolia = 4_000

    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(item) {
            pattern("#U#")
            pattern("L*R")
            pattern("#D#")
            input('#', HaimeviskaBlockCard.LOG.item)
            input('U', Items.LANTERN)
            input('D', ItemTags.WOOL_CARPETS)
            input('L', ConventionalItemTags.GLASS_PANES)
            input('R', ItemTags.WOODEN_DOORS)
            input('*', MaterialCard.FAIRY_CRYSTAL.item)
        } on MaterialCard.FAIRY_CRYSTAL.item
    }
}

class FairyHouseBlock(card: FairyHouseCard) : FairyFactoryBlock(card)

class FairyHouseBlockEntity(card: FairyHouseCard, pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyHouseBlockEntity>(card, pos, state) {
    override fun getThis() = this
    override fun serverTick(world: World, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        if (folia < 1_000) {
            setStatus(FairyFactoryBlock.Status.OFFLINE)
            return
        }

        folia -= 10

        setStatus(FairyFactoryBlock.Status.PROCESSING)
    }
}

class FairyHouseScreenHandler(card: FairyHouseCard, arguments: Arguments) : FairyFactoryScreenHandler(card, arguments)
