package miragefairy2024.mod.fairyhouse

import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.util.collectItem
import miragefairy2024.util.insertItem
import miragefairy2024.util.inventoryAccessor
import miragefairy2024.util.itemStacks
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockBox
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
            AbstractFairyHouseBlockEntity.SlotSettings(0, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { true }, // 机
            AbstractFairyHouseBlockEntity.SlotSettings(0, 26) { FairyCollectorCard.isValid(it) }, // 回収妖精
            AbstractFairyHouseBlockEntity.SlotSettings(20, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { true }, // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(20, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { true }, // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(20, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { true }, // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(20, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { true }, // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(0, 26, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { FairyCollectorCard.isValid(it) }, // 仕分け妖精
        ),
    ),
) {
    private fun isValid(itemStack: ItemStack): Boolean {
        if (!itemStack.isOf(FairyCard.item)) return false
        val motif = itemStack.getFairyMotif() ?: return false
        return when (motif) {
            MotifCard.HOPPER, MotifCard.ENDERMAN -> true // TODO 系統
            else -> false
        }
    }

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

    private var cooldown = -1

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)
        if (cooldown > 0) {
            cooldown--
        } else if (cooldown <= -1) {
            cooldown = world.random.nextInt(20 * 10)
        } else {
            cooldown = 20 * 10

            var folia = getFolia()
            val status = run {
                if (folia < 10) return@run FairyFactoryBlock.Status.OFFLINE // フォリアが足りない

                if (!itemStacks.any { it.isEmpty }) return@run FairyFactoryBlock.Status.IDLE // 負荷軽減のために1スロも空いていない場合は止める

                var collected = false
                val region = BlockBox(pos.x - 10, pos.y - 4, pos.z - 10, pos.x + 10, pos.y, pos.z + 10)
                collectItem(world, pos, maxCount = 2, reach = 30, region = region, ignoreOriginalWall = true) {
                    if (folia < 10) return@collectItem false
                    val itemStack = it.stack.copy()
                    if (inventoryAccessor.insertItem(itemStack, 0 until 4)) {
                        folia -= 10
                        collected = true
                    }
                    if (itemStack.isEmpty) {
                        it.discard()
                    } else {
                        it.stack = itemStack
                    }
                    true
                }

                if (collected) {
                    FairyFactoryBlock.Status.PROCESSING
                } else {
                    FairyFactoryBlock.Status.IDLE
                }
            }
            setFolia(folia)
            setStatus(status)

        }
    }
}
