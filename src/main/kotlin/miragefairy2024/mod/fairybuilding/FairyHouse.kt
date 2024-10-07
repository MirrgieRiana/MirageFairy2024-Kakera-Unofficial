package miragefairy2024.mod.fairybuilding

import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object FairyHouseConfiguration : FairyFactoryConfiguration<FairyHouseBlockEntity, FairyFactoryScreenHandler>() {
    override val path = "fairy_house"
    override val tier = 2
    override val name = EnJa("Fairy House", "妖精の家")
    override val poem = EnJa("Home sweet home", "あたたかいおうち")

    override fun createBlock(settings: FabricBlockSettings) = FairyFactoryBlock({ FairyHouseCard }, settings)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyHouseBlockEntity)

    override fun createScreenHandler(arguments: FairyBuildingScreenHandler.Arguments) = FairyFactoryScreenHandler(FairyHouseCard, arguments)

    override val guiWidth = 176
    override val guiHeight = 178

    override fun createSlots(): List<SlotConfiguration> {
        return super.createSlots() + listOf(
            SlotConfiguration(8, 39, appearance = Appearance(false, listOf(Position(10.0, 9.5, 14.5, 0.0F, 180.0F, 200)))), // 本棚の上
            SlotConfiguration(61, 17, appearance = Appearance(true, listOf(Position(4.5, 2.2, 14.0, 90.0F, 270.0F, 200)))) { it.isOf(FairyCard.item) }, // ベッド
            SlotConfiguration(61, 41, appearance = Appearance(true, listOf(Position(4.5, 7.2, 14.0, 90.0F, 270.0F, 200)))) { it.isOf(FairyCard.item) }, // ベッド
            SlotConfiguration(61, 65, appearance = Appearance(true, listOf(Position(4.5, 12.2, 14.0, 90.0F, 270.0F, 200)))) { it.isOf(FairyCard.item) }, // ベッド
            SlotConfiguration(82, 48, appearance = Appearance(false, listOf(Position(1.5, 5.5, 2.0, 0.0F, 270.0F, 200)))), // タンスの上
            SlotConfiguration(104, 27, appearance = Appearance(true, listOf(Position(12.0, 0.1, 3.0, 0.0F, 280.0F, 200)))) { it.isOf(FairyCard.item) }, // テーブル
            SlotConfiguration(128, 17, appearance = Appearance(false, listOf(Position(11.5, 1.5, 7.5, 0.0F, 270.0F, 200)))), // テーブルの上
            SlotConfiguration(152, 17, appearance = Appearance(true, listOf(Position(13.0, 0.1, 12.0, 0.0F, 70.0F, 200)))) { it.isOf(FairyCard.item) }, // テーブル
            SlotConfiguration(152, 37, appearance = Appearance(true, listOf(Position(9.0, 0.1, 12.0, 0.0F, 110.0F, 200)))) { it.isOf(FairyCard.item) }, // テーブル
            SlotConfiguration(108, 60, appearance = Appearance(true, run {
                listOf(
                    Position(2.0, 0.1, 10.0, 0.0F, 110.0F, 140),
                    Position(6.0, 0.1, 10.0, 0.0F, 60.0F, 140),
                )
            })) { it.isOf(FairyCard.item) }, // 床（左下）
            SlotConfiguration(
                128, 60,
                appearance = Appearance(false, listOf(Position(4.0, 0.1, 7.0, 0.0F, 90.0F, 200))),
                insertDirections = setOf(Direction.UP, Direction.WEST),
                extractDirections = setOf(Direction.DOWN, Direction.EAST),
            ), // 床の上
            SlotConfiguration(148, 60, appearance = Appearance(true, run {
                listOf(
                    Position(5.0, 0.1, 4.0, 0.0F, 290.0F, 180),
                    Position(7.0, 0.1, 6.0, 0.0F, 320.0F, 180),
                )
            })) { it.isOf(FairyCard.item) }, // 床（右上）
        )
    }

    override val collectingFolia = 2_000
    override val maxFolia = 4_000
}

object FairyHouseCard : FairyFactoryCard<FairyHouseConfiguration, FairyHouseBlockEntity, FairyFactoryScreenHandler>(FairyHouseConfiguration) {
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

class FairyHouseBlockEntity(pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyHouseBlockEntity>(FairyHouseCard, pos, state) {
    override val self = this
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
