package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos

object FairyActiveConsumerCard : FairyLogisticsCard<FairyActiveConsumerBlock, FairyActiveConsumerBlockEntity, FairyActiveConsumerScreenHandler>() {
    override fun getPath() = "fairy_active_consumer"
    override val tier = 3
    override val name = EnJa("Fairy Active Consumer", "妖精の郵便受け")
    override val poem = EnJa("Tonight, I'll Be Eating...", "焼き鯖だよ――")
    override val description = EnJa("The ordered items are delivered", "注文したアイテムが搬入される")
    override fun createBlock() = FairyActiveConsumerBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyActiveConsumerBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyActiveConsumerScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 178
    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(item) {
            pattern("#A#")
            pattern("DCD")
            pattern("###")
            input('A', BlockMaterialCard.AURA_STONE.item)
            input('#', Items.IRON_INGOT)
            input('C', Items.ITEM_FRAME)
            input('D', Items.LIGHT_BLUE_DYE)
        } on BlockMaterialCard.AURA_STONE.item
    }
}

class FairyActiveConsumerBlock(card: FairyActiveConsumerCard) : FairyLogisticsBlock(card)

class FairyActiveConsumerBlockEntity(card: FairyActiveConsumerCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyActiveConsumerBlockEntity>(card, pos, state) {
    override fun getThis() = this
}

class FairyActiveConsumerScreenHandler(card: FairyActiveConsumerCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
