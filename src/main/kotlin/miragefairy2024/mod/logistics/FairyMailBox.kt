package miragefairy2024.mod.logistics

import miragefairy2024.ModContext
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.MapColor
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup

object FairyMailboxConfiguration : FairyLogisticsBlockConfiguration() {
    override val path = "fairy_mailbox"
    override val name = EnJa("Fairy Mailbox", "妖精の郵便受け")
    override val tier = 3
    override val poem = EnJa("TODO", "TODO") // TODO
    override fun createBlock() = FairyMailboxBlock(createFairyLogisticsBlockSettings().mapColor(MapColor.PALE_PURPLE).sounds(BlockSoundGroup.METAL))

    context(ModContext)
    override fun init(card: FairyLogisticsBlockCard) {
        super.init(card)

        registerShapedRecipeGeneration(card.item) {
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

object FairyMailboxCard : FairyLogisticsBlockCard(FairyMailboxConfiguration)

class FairyMailboxBlock(settings: Settings) : FairyLogisticsBlock(settings) { // TODO 上下面にも

    // TODO
    //override fun createBlockEntity(pos: BlockPos, state: BlockState) = FairyStatueBlockEntity(card, pos, state)
    /*
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world.isClient) return
        val blockEntity = world.getBlockEntity(pos) as? FairyStatueBlockEntity ?: return
        blockEntity.setMotif(itemStack.getFairyStatueMotif())
    }
    */

}
