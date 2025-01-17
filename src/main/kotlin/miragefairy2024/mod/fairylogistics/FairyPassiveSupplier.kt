package miragefairy2024.mod.fairylogistics

import miragefairy2024.lib.MachineScreenHandler
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object FairyPassiveSupplierCard : FairyLogisticsCard<FairyPassiveSupplierBlock, FairyPassiveSupplierBlockEntity, FairyPassiveSupplierScreenHandler>() {
    override fun getPath() = "fairy_passive_supplier"
    override fun createBlock() = FairyPassiveSupplierBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyPassiveSupplierBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyPassiveSupplierScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 178
}

class FairyPassiveSupplierBlock(card: FairyPassiveSupplierCard) : FairyLogisticsBlock(card)

class FairyPassiveSupplierBlockEntity(card: FairyPassiveSupplierCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyPassiveSupplierBlockEntity>(card, pos, state) {
    override fun getThis() = this
    override fun getActualSide(side: Direction) = TODO()
}

class FairyPassiveSupplierScreenHandler(card: FairyPassiveSupplierCard, arguements: Arguments) : FairyLogisticsScreenHandler(card, arguements)
