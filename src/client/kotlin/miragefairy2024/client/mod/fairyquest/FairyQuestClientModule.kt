package miragefairy2024.client.mod.fairyquest

import miragefairy2024.api.client.RenderItemHandler
import miragefairy2024.client.util.stack
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.fairyquest.fairyQuestCardScreenHandlerType
import miragefairy2024.mod.fairyquest.getFairyQuestRecipe
import miragefairy2024.util.createItemStack
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.gui.screens.MenuScreens as HandledScreens
import net.minecraft.world.item.ItemDisplayContext as ModelTransformationMode
import net.minecraft.client.resources.model.ModelResourceLocation as ModelIdentifier
import net.minecraft.world.item.Items
import net.minecraft.util.Mth as MathHelper
import com.mojang.math.Axis as RotationAxis

fun initFairyQuestClientModule() {
    RenderItemHandler.listeners += RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
        if (!stack.`is`(FairyQuestCardCard.item)) return@RenderItemHandler
        matrices.stack {
            model.transformation.getTransformation(renderMode).apply(leftHanded, matrices)

            // TODO モデルに
            matrices.stack {
                matrices.translate(0F, 1F / 16F, 0F)
                matrices.scale(0.5F, 0.5F, 0.01F)
                val resultModel = MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier("minecraft", "nether_portal", "axis=x"))
                MinecraftClient.getInstance().itemRenderer.renderItem(Items.DIRT.createItemStack(), ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
            }

            val recipe = stack.getFairyQuestRecipe()
            if (recipe != null) {
                matrices.stack {
                    matrices.translate(0F, 1F / 16F, 0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    val resultModel = MinecraftClient.getInstance().itemRenderer.getModel(recipe.icon, null, null, 0)
                    MinecraftClient.getInstance().itemRenderer.renderItem(recipe.icon, ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
                matrices.stack {
                    matrices.translate(0F, 1F / 16F, -0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathHelper.TWO_PI * 0.5F))
                    val resultModel = MinecraftClient.getInstance().itemRenderer.getModel(recipe.icon, null, null, 0)
                    MinecraftClient.getInstance().itemRenderer.renderItem(recipe.icon, ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
            }

        }
    }
    HandledScreens.register(fairyQuestCardScreenHandlerType) { gui, inventory, title -> FairyQuestCardScreen(gui, inventory, title) }
}
