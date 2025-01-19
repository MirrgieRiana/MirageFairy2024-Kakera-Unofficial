package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.item.Items
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos

object FairyPassiveSupplierCard : FairyLogisticsCard<FairyPassiveSupplierBlock, FairyPassiveSupplierBlockEntity, FairyPassiveSupplierScreenHandler>() {
    override fun getPath() = "fairy_passive_supplier"
    override val tier = 3
    override val name = EnJa("Fairy Passive Supplier", "妖精の郵便屋さん")
    override val poem = EnJa("Fairies' Delivery Service", "落ち込んだりもしたけれど、私は元気です。")
    override val description = EnJa("Accepts and delivers orders", "注文を受け付けて配達する")
    override fun createBlockSettings() = super.createBlockSettings().mapColor(MapColor.PINK).sounds(BlockSoundGroup.WOOD)
    override fun createBlock() = FairyPassiveSupplierBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyPassiveSupplierBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyPassiveSupplierScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 178
    context(ModContext)
    override fun init() {
        super.init()

        block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }

        registerShapedRecipeGeneration(item) {
            pattern("#A#")
            pattern("DCD")
            pattern("###")
            input('A', BlockMaterialCard.AURA_STONE.item)
            input('#', ItemTags.PLANKS)
            input('C', Items.BARREL)
            input('D', Items.PINK_DYE)
        } on BlockMaterialCard.AURA_STONE.item
    }
}

class FairyPassiveSupplierBlock(card: FairyPassiveSupplierCard) : FairyLogisticsBlock(card)

class FairyPassiveSupplierBlockEntity(card: FairyPassiveSupplierCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyPassiveSupplierBlockEntity>(card, pos, state) {
    override fun getThis() = this
}

class FairyPassiveSupplierScreenHandler(card: FairyPassiveSupplierCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
