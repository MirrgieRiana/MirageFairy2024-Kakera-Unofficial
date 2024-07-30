package miragefairy2024.mod.fairyhouse

import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object FairyCollectorCard : FairyFactoryCard<FairyCollectorBlockEntity, FairyFactoryScreenHandler>(
    "fairy_collector", 2, "Fairy Collector", "エンデルマーニャの隠れ家",
    "TODO", "TODO", // TODO
    { FairyFactoryBlock({ FairyCollectorCard }, it) },
    BlockEntityAccessor(::FairyCollectorBlockEntity),
    { FairyFactoryScreenHandler(FairyCollectorCard, it) },
    176, 180,
    AbstractFairyHouseBlockEntity.Settings(
        slots = listOf(
            AbstractFairyHouseBlockEntity.SlotSettings(0, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings(20, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            AbstractFairyHouseBlockEntity.SlotSettings(40, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // ベッド
            *(0 until 9).map { c ->
                AbstractFairyHouseBlockEntity.SlotSettings(18 * c, 50, appearance = AbstractFairyHouseBlockEntity.Appearance(10.0, 9.5, 14.5, 0.0, 180.0))
            }.toTypedArray()
        ),
    ),
) {
    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(FairyCollectorCard.item) {
            pattern("BCB")
            pattern("C#C")
            pattern("BCB")
            input('#', FairyHouseCard.item)
            input('C', Items.CHEST)
            input('B', Items.BOWL)
        } on FairyHouseCard.item
    }
}

class FairyCollectorBlockEntity(pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyCollectorBlockEntity>(FairyCollectorCard, pos, state) {
    override val self = this

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)
        if (folia >= 100) {
            folia -= world.random.nextBetween(1, 100)
        }
    }
}
