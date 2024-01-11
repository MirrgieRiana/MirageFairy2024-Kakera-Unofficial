package miragefairy2024.api.client

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack

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
