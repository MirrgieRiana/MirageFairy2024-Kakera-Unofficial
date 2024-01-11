package miragefairy2024.client.mod.fairyquest

import miragefairy2024.api.client.RenderItemHandler
import miragefairy2024.client.util.pushAndPop
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.fairyquest.fairyQuestCardScreenHandlerType
import miragefairy2024.mod.fairyquest.getFairyQuestRecipe
import miragefairy2024.util.createItemStack
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.Items
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis

fun initFairyQuestClientModule() {
    RenderItemHandler.listeners += RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
        if (!stack.isOf(FairyQuestCardCard.item)) return@RenderItemHandler
        matrices.pushAndPop {
            model.transformation.getTransformation(renderMode).apply(leftHanded, matrices)

            // TODO モデルに
            matrices.pushAndPop {
                matrices.translate(0F, 1F / 16F, 0F)
                matrices.scale(0.5F, 0.5F, 0.01F)
                val resultModel = MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier("minecraft", "nether_portal", "axis=x"))
                MinecraftClient.getInstance().itemRenderer.renderItem(Items.DIRT.createItemStack(), ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
            }

            val recipe = stack.getFairyQuestRecipe()
            if (recipe != null) {
                matrices.pushAndPop {
                    matrices.translate(0F, 1F / 16F, 0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    val resultModel = MinecraftClient.getInstance().itemRenderer.getModel(recipe.icon, null, null, 0)
                    MinecraftClient.getInstance().itemRenderer.renderItem(recipe.icon, ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
                matrices.pushAndPop {
                    matrices.translate(0F, 1F / 16F, -0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathHelper.TAU * 0.5F))
                    val resultModel = MinecraftClient.getInstance().itemRenderer.getModel(recipe.icon, null, null, 0)
                    MinecraftClient.getInstance().itemRenderer.renderItem(recipe.icon, ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
            }

        }
    }
    HandledScreens.register(fairyQuestCardScreenHandlerType) { gui, inventory, title -> FairyQuestCardScreen(gui, inventory, title) }
}
