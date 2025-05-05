package miragefairy2024.mixin.client.api

import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.item.ItemStack
import com.mojang.blaze3d.vertex.PoseStack as MatrixStack
import net.minecraft.client.renderer.MultiBufferSource as VertexConsumerProvider
import net.minecraft.world.item.ItemDisplayContext as ModelTransformationMode

fun interface RenderItemHandler {
    companion object {
        val INSTANCE = RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
            listeners.forEach {
                it.renderItem(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model)
            }
        }
        val listeners = mutableListOf<RenderItemHandler>()
    }

    fun renderItem(stack: ItemStack, renderMode: ModelTransformationMode, leftHanded: Boolean, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int, model: BakedModel)
}
