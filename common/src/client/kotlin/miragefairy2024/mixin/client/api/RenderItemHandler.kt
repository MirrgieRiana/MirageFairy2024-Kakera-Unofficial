package miragefairy2024.mixin.client.api

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

fun interface RenderItemHandler {
    companion object {
        val INSTANCE = RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
            listeners.forEach {
                it.renderItem(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model)
            }
        }
        val listeners = mutableListOf<RenderItemHandler>()
    }

    fun renderItem(stack: ItemStack, renderMode: ItemDisplayContext, leftHanded: Boolean, matrices: PoseStack, vertexConsumers: MultiBufferSource, light: Int, overlay: Int, model: BakedModel)
}
