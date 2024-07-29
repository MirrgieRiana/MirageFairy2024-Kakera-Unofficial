package miragefairy2024.mod.fairyhouse

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object FairyHouseCard : FairyFactoryCard<FairyHouseBlockEntity, FairyHouseScreenHandler>(
    "fairy_house", 2, "Fairy House", "妖精の家",
    "Home sweet home", "あたたかいおうち",
    { AbstractFairyHouseBlock({ FairyHouseCard }, it.luminance { 5 }) },
    BlockEntityAccessor(::FairyHouseBlockEntity),
    ::FairyHouseScreenHandler,
    176, 180,
    AbstractFairyHouseBlockEntity.Settings(
        slots = listOf(
            AbstractFairyHouseBlockEntity.SlotSettings(8, 40, appearance = AbstractFairyHouseBlockEntity.Appearance(10.0, 9.5, 14.5, 0.0, 180.0)), // 本棚の上
            AbstractFairyHouseBlockEntity.SlotSettings(61, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings(61, 46, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 7.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings(61, 66, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 12.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings(82, 49, appearance = AbstractFairyHouseBlockEntity.Appearance(1.5, 5.5, 2.0, 0.0, 90.0)), // タンスの上
            AbstractFairyHouseBlockEntity.SlotSettings(104, 28, appearance = AbstractFairyHouseBlockEntity.Appearance(12.0, 0.1, 3.0, 0.0, 280.0)) { it.isOf(FairyCard.item) }, // テーブル
            AbstractFairyHouseBlockEntity.SlotSettings(128, 18, appearance = AbstractFairyHouseBlockEntity.Appearance(11.5, 1.5, 7.5, 0.0, 270.0)), // テーブルの上
            AbstractFairyHouseBlockEntity.SlotSettings(152, 18, appearance = AbstractFairyHouseBlockEntity.Appearance(13.0, 0.1, 12.0, 0.0, 70.0)) { it.isOf(FairyCard.item) }, // テーブル
            AbstractFairyHouseBlockEntity.SlotSettings(152, 38, appearance = AbstractFairyHouseBlockEntity.Appearance(9.0, 0.1, 12.0, 0.0, 110.0)) { it.isOf(FairyCard.item) }, // テーブル
            AbstractFairyHouseBlockEntity.SlotSettings(108, 61, appearance = AbstractFairyHouseBlockEntity.Appearance(2.0, 0.1, 10.0, 0.0, 110.0)) { it.isOf(FairyCard.item) }, // 床
            AbstractFairyHouseBlockEntity.SlotSettings(
                128, 61,
                appearance = AbstractFairyHouseBlockEntity.Appearance(4.0, 0.1, 7.0, 0.0, 90.0),
                insertDirections = setOf(Direction.UP, Direction.WEST),
                extractDirections = setOf(Direction.DOWN, Direction.EAST),
            ), // 床の上
            AbstractFairyHouseBlockEntity.SlotSettings(148, 61, appearance = AbstractFairyHouseBlockEntity.Appearance(5.0, 0.1, 4.0, 0.0, 290.0)) { it.isOf(FairyCard.item) }, // 床
        ),
    ),
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

class FairyHouseBlockEntity(pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyHouseBlockEntity>(FairyHouseCard, pos, state) {
    override val self = this

    override fun renderExtra(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val i = (light and 0x0000FF) or 0xF00000
        renderingProxy.renderCutoutBlock(Identifier(MirageFairy2024.modId, "block/fairy_house/lantern"), null, 1.0F, 1.0F, 1.0F, i, overlay)
    }

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)
        if (folia >= 100) {
            folia -= world.random.nextBetween(1, 100)
        }
    }
}

class FairyHouseScreenHandler(arguments: Arguments) : FairyFactoryScreenHandler(FairyHouseCard, arguments)
