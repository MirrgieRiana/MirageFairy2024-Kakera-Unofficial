package miragefairy2024.client.mod

import miragefairy2024.api.client.RenderItemHandler
import miragefairy2024.client.util.stack
import miragefairy2024.mod.fairy.FairyStatueBlockItem
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getFairyStatueMotif
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.world.item.ItemDisplayContext as ModelTransformationMode


fun initFairyStatueClientModule() {
    RenderItemHandler.listeners += RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
        if (stack.item !is FairyStatueBlockItem) return@RenderItemHandler
        val motif = stack.getFairyStatueMotif()
        val itemStack = motif?.createFairyItemStack() ?: return@RenderItemHandler
        matrices.stack {
            model.transforms.getTransform(renderMode).apply(leftHanded, matrices)

            matrices.stack {
                matrices.scale(0.5F, 0.5F, 0.5F)
                val resultModel = MinecraftClient.getInstance().itemRenderer.getModel(itemStack, null, null, 0)
                MinecraftClient.getInstance().itemRenderer.render(itemStack, ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
            }
        }
    }
}
