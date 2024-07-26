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
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.Items
import net.minecraft.registry.tag.ItemTags
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object FairyHouseCard : AbstractFairyHouseCard<AbstractFairyHouseBlock<FairyHouseBlockEntity>, FairyHouseBlockEntity>(
    "fairy_house", 2, "Fairy House", "妖精の家",
    "Home sweet home", "あたたかいおうち",
    { AbstractFairyHouseBlock({ FairyHouseCard }, it.luminance { 5 }) },
    BlockEntityAccessor(::FairyHouseBlockEntity),
    ::FairyHouseScreenHandler,
    AbstractFairyHouseBlockEntity.Settings(
        slots = listOf(
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings(), // タンスの上
            AbstractFairyHouseBlockEntity.SlotSettings(
                insertDirections = setOf(Direction.UP, Direction.WEST),
                extractDirections = setOf(Direction.DOWN, Direction.EAST),
            ), // 床の上
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // 床
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // 床
            AbstractFairyHouseBlockEntity.SlotSettings(), // 本棚の上
            AbstractFairyHouseBlockEntity.SlotSettings(), // テーブルの上
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // テーブル
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // テーブル
            AbstractFairyHouseBlockEntity.SlotSettings() { it.isOf(FairyCard.item) }, // テーブル
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

class FairyHouseBlockEntity(pos: BlockPos, state: BlockState) : AbstractFairyHouseBlockEntity<FairyHouseBlockEntity>(FairyHouseCard, pos, state) {
    override fun renderExtra(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val i = (light and 0x0000FF) or 0xF00000
        renderingProxy.renderCutoutBlock(Identifier(MirageFairy2024.modId, "block/fairy_house/lantern"), null, 1.0F, 1.0F, 1.0F, i, overlay)
    }
}

class FairyHouseScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    inventory: Inventory,
    propertyDelegate: PropertyDelegate,
    context: ScreenHandlerContext,
) : AbstractFairyHouseScreenHandler<FairyHouseBlockEntity>(
    FairyHouseCard,
    syncId,
    playerInventory,
    inventory,
    propertyDelegate,
    context,
)
