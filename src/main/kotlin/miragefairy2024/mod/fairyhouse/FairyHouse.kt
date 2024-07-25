package miragefairy2024.mod.fairyhouse

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.createItemStack
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import kotlin.jvm.optionals.getOrNull

object FairyHouseCard : AbstractFairyHouseCard<FairyHouseBlock, FairyHouseBlockEntity>(
    "fairy_house", 2, "Fairy House", "妖精の家",
    "TODO", "TODO", // TODO
    { FairyHouseBlock(it.luminance { 5 }) },
    { pos, state -> FairyHouseBlockEntity(pos, state) },
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

class FairyHouseBlock(settings: Settings) : AbstractFairyHouseBlock(settings) {
    override fun createBlockEntity(pos: BlockPos, state: BlockState) = FairyHouseBlockEntity(pos, state)
}

class FairyHouseBlockEntity(pos: BlockPos, state: BlockState) : AbstractFairyHouseBlockEntity(FairyHouseCard.blockEntityType, pos, state) {

    private val inventory = DefaultedList.ofSize(4, EMPTY_ITEM_STACK)

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)


        //nbt.wrapper["ItemStack"].set(itemStack.toNbt())
    }

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val world = world ?: return
        val blockState = world.getBlockState(pos)
        if (!blockState.isOf(FairyHouseCard.block)) return
        val direction = blockState.getOrEmpty(HorizontalFacingBlock.FACING).getOrNull() ?: return

        renderingProxy.stack {
            renderingProxy.translate(0.5, 0.5, 0.5)
            renderingProxy.rotateY(-direction.asRotation() / 180F * Math.PI.toFloat())
            renderingProxy.translate(-0.5, -0.5, -0.5)

            renderingProxy.stack {
                renderingProxy.translate(0.2, 0.01, 0.3)
                renderingProxy.rotateY(126.0F / 180F * 3.14F)
                renderingProxy.scale(0.5F, 0.5F, 0.5F)
                renderingProxy.translate(0.0, 2.0 / 16.0, 0.0)
                renderingProxy.renderItemStack(MotifCard.IRON.createFairyItemStack())
            }
            renderingProxy.stack {
                renderingProxy.translate(0.7, 0.01, 0.4)
                renderingProxy.rotateY(42.0F / 180F * 3.14F)
                renderingProxy.scale(0.5F, 0.5F, 0.5F)
                renderingProxy.translate(0.0, 2.0 / 16.0, 0.0)
                renderingProxy.renderItemStack(MotifCard.MAGENTA_GLAZED_TERRACOTTA.createFairyItemStack())
            }
            renderingProxy.stack {
                renderingProxy.translate(0.3, 0.01, 0.8)
                renderingProxy.rotateY(235.0F / 180F * 3.14F)
                renderingProxy.scale(0.5F, 0.5F, 0.5F)
                renderingProxy.translate(0.0, 2.0 / 16.0, 0.0)
                renderingProxy.renderItemStack(MotifCard.DIAMOND.createFairyItemStack())
            }

            renderingProxy.stack {
                renderingProxy.translate(4.5 / 16.0, 2.5 / 16.0, 8.5 / 16.0)
                renderingProxy.rotateY(90.0F / 180F * 3.14F)
                renderingProxy.scale(0.5F, 0.5F, 0.5F)
                renderingProxy.translate(0.0, 0.0 / 16.0, 0.0)
                renderingProxy.renderItemStack(Items.CAKE.createItemStack())
            }

            val i = (light and 0x0000FF) or 0xF00000
            renderingProxy.renderCutoutBlock(Identifier(MirageFairy2024.modId, "block/fairy_house/lantern"), null, 1.0F, 1.0F, 1.0F, i, overlay)

        }
    }

}
