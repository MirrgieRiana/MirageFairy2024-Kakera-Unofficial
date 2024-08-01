package miragefairy2024.mod.fairybuilding

import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
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

object FairyHouseSettings : FairyFactorySettings<FairyHouseBlockEntity, FairyFactoryScreenHandler>() {
    override val path = "fairy_house"
    override val tier = 2
    override val enName = "Fairy House"
    override val jaName = "妖精の家"
    override val enPoem = "Home sweet home"
    override val jaPoem = "あたたかいおうち"

    override fun createBlock(settings: FabricBlockSettings) = FairyFactoryBlock({ FairyHouseCard }, settings)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyHouseBlockEntity)

    override fun createScreenHandler(arguments: FairyBuildingScreenHandler.Arguments) = FairyFactoryScreenHandler(FairyHouseCard, arguments)

    override fun createSlots(): List<SlotSettings> {
        return super.createSlots() + listOf(
            SlotSettings(8, 39, appearance = Appearance(10.0, 9.5, 14.5, 0.0, 180.0)), // 本棚の上
            SlotSettings(61, 17, appearance = Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            SlotSettings(61, 41, appearance = Appearance(4.5, 7.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            SlotSettings(61, 65, appearance = Appearance(4.5, 12.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            SlotSettings(82, 48, appearance = Appearance(1.5, 5.5, 2.0, 0.0, 90.0)), // タンスの上
            SlotSettings(104, 27, appearance = Appearance(12.0, 0.1, 3.0, 0.0, 280.0)) { it.isOf(FairyCard.item) }, // テーブル
            SlotSettings(128, 17, appearance = Appearance(11.5, 1.5, 7.5, 0.0, 270.0)), // テーブルの上
            SlotSettings(152, 17, appearance = Appearance(13.0, 0.1, 12.0, 0.0, 70.0)) { it.isOf(FairyCard.item) }, // テーブル
            SlotSettings(152, 37, appearance = Appearance(9.0, 0.1, 12.0, 0.0, 110.0)) { it.isOf(FairyCard.item) }, // テーブル
            SlotSettings(108, 60, appearance = Appearance(2.0, 0.1, 10.0, 0.0, 110.0)) { it.isOf(FairyCard.item) }, // 床
            SlotSettings(
                128, 60,
                appearance = Appearance(4.0, 0.1, 7.0, 0.0, 90.0),
                insertDirections = setOf(Direction.UP, Direction.WEST),
                extractDirections = setOf(Direction.DOWN, Direction.EAST),
            ), // 床の上
            SlotSettings(148, 60, appearance = Appearance(5.0, 0.1, 4.0, 0.0, 290.0)) { it.isOf(FairyCard.item) }, // 床
        )
    }

    override val guiWidth = 176
    override val guiHeight = 178

    override val collectingFolia = 2_000
    override val maxFolia = 4_000
}

object FairyHouseCard : FairyFactoryCard<FairyHouseSettings, FairyHouseBlockEntity, FairyFactoryScreenHandler>(FairyHouseSettings) {
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
