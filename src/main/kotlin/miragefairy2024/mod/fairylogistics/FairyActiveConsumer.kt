package miragefairy2024.mod.fairylogistics

import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.util.EnJa
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object FairyActiveConsumerCard : FairyLogisticsCard<FairyActiveConsumerBlock, FairyActiveConsumerBlockEntity, FairyActiveConsumerScreenHandler>() {
    override fun getPath() = "fairy_active_consumer"
    override val tier = 3
    override val name = EnJa("Fairy Active Consumer", "妖精の郵便受け")
    override val poem = EnJa()
    override val description = EnJa("The ordered items are delivered", "注文したアイテムが搬入される")
    override fun createBlock() = FairyActiveConsumerBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyActiveConsumerBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyActiveConsumerScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 178
}

class FairyActiveConsumerBlock(card: FairyActiveConsumerCard) : FairyLogisticsBlock(card)

class FairyActiveConsumerBlockEntity(card: FairyActiveConsumerCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyActiveConsumerBlockEntity>(card, pos, state) {
    override fun getThis() = this
    override fun getActualSide(side: Direction) = TODO()
}

class FairyActiveConsumerScreenHandler(card: FairyActiveConsumerCard, arguements: Arguments) : FairyLogisticsScreenHandler(card, arguements)
