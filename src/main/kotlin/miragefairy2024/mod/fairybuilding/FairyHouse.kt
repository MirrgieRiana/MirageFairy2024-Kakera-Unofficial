package miragefairy2024.mod.fairybuilding

import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object FairyHouseCard : FairyFactoryCard<FairyHouseBlockEntity, FairyFactoryScreenHandler>(
    "fairy_house", 2, "Fairy House", "妖精の家",
    "Home sweet home", "あたたかいおうち",
    { FairyFactoryBlock({ FairyHouseCard }, it) },
    BlockEntityAccessor(::FairyHouseBlockEntity),
    { FairyFactoryScreenHandler(FairyHouseCard, it) },
    176, 178,
    FairyBuildingBlockEntity.Settings(
        slots = listOf(
            FairyBuildingBlockEntity.SlotSettings(8, 39, appearance = FairyBuildingBlockEntity.Appearance(10.0, 9.5, 14.5, 0.0, 180.0)), // 本棚の上
            FairyBuildingBlockEntity.SlotSettings(61, 17, appearance = FairyBuildingBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            FairyBuildingBlockEntity.SlotSettings(61, 41, appearance = FairyBuildingBlockEntity.Appearance(4.5, 7.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            FairyBuildingBlockEntity.SlotSettings(61, 65, appearance = FairyBuildingBlockEntity.Appearance(4.5, 12.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            FairyBuildingBlockEntity.SlotSettings(82, 48, appearance = FairyBuildingBlockEntity.Appearance(1.5, 5.5, 2.0, 0.0, 90.0)), // タンスの上
            FairyBuildingBlockEntity.SlotSettings(104, 27, appearance = FairyBuildingBlockEntity.Appearance(12.0, 0.1, 3.0, 0.0, 280.0)) { it.isOf(FairyCard.item) }, // テーブル
            FairyBuildingBlockEntity.SlotSettings(128, 17, appearance = FairyBuildingBlockEntity.Appearance(11.5, 1.5, 7.5, 0.0, 270.0)), // テーブルの上
            FairyBuildingBlockEntity.SlotSettings(152, 17, appearance = FairyBuildingBlockEntity.Appearance(13.0, 0.1, 12.0, 0.0, 70.0)) { it.isOf(FairyCard.item) }, // テーブル
            FairyBuildingBlockEntity.SlotSettings(152, 37, appearance = FairyBuildingBlockEntity.Appearance(9.0, 0.1, 12.0, 0.0, 110.0)) { it.isOf(FairyCard.item) }, // テーブル
            FairyBuildingBlockEntity.SlotSettings(108, 60, appearance = FairyBuildingBlockEntity.Appearance(2.0, 0.1, 10.0, 0.0, 110.0)) { it.isOf(FairyCard.item) }, // 床
            FairyBuildingBlockEntity.SlotSettings(
                128, 60,
                appearance = FairyBuildingBlockEntity.Appearance(4.0, 0.1, 7.0, 0.0, 90.0),
                insertDirections = setOf(Direction.UP, Direction.WEST),
                extractDirections = setOf(Direction.DOWN, Direction.EAST),
            ), // 床の上
            FairyBuildingBlockEntity.SlotSettings(148, 60, appearance = FairyBuildingBlockEntity.Appearance(5.0, 0.1, 4.0, 0.0, 290.0)) { it.isOf(FairyCard.item) }, // 床
        ),
    ),
    2_000, 4_000,
) {
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

class FairyHouseBlockEntity(pos: BlockPos, state: BlockState) :
    FairyFactoryBlockEntity<FairyHouseBlockEntity>(FairyHouseCard, pos, state) {

    override val self = this


    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)

        if (folia < 1_000) {
            setStatus(FairyFactoryBlock.Status.OFFLINE)
            return
        }

        folia -= 10

        setStatus(FairyFactoryBlock.Status.PROCESSING)
    }

}
