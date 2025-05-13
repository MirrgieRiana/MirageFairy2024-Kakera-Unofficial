package miragefairy2024.client.mod.fairyquest

import miragefairy2024.ModContext
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.stack
import miragefairy2024.mixin.client.api.RenderItemHandler
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.fairyquest.fairyQuestCardScreenHandlerType
import miragefairy2024.mod.fairyquest.getFairyQuestRecipe
import miragefairy2024.util.createItemStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import com.mojang.math.Axis as RotationAxis
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.resources.model.ModelResourceLocation as ModelIdentifier
import net.minecraft.util.Mth as MathHelper
import net.minecraft.world.item.ItemDisplayContext as ModelTransformationMode

context(ModContext)
fun initFairyQuestClientModule() {
    RenderItemHandler.listeners += RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
        if (!stack.`is`(FairyQuestCardCard.item())) return@RenderItemHandler
        matrices.stack {
            model.transforms.getTransform(renderMode).apply(leftHanded, matrices)

            // TODO モデルに
            matrices.stack {
                matrices.translate(0F, 1F / 16F, 0F)
                matrices.scale(0.5F, 0.5F, 0.01F)
                val resultModel = MinecraftClient.getInstance().modelManager.getModel(ModelIdentifier(ResourceLocation.fromNamespaceAndPath("minecraft", "nether_portal"), "axis=x"))
                MinecraftClient.getInstance().itemRenderer.render(Items.DIRT.createItemStack(), ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
            }

            val recipe = stack.getFairyQuestRecipe()
            if (recipe != null) {
                matrices.stack {
                    matrices.translate(0F, 1F / 16F, 0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    val resultModel = MinecraftClient.getInstance().itemRenderer.getModel(recipe.icon(), null, null, 0)
                    MinecraftClient.getInstance().itemRenderer.render(recipe.icon(), ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
                matrices.stack {
                    matrices.translate(0F, 1F / 16F, -0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    matrices.mulPose(RotationAxis.YP.rotation(MathHelper.TWO_PI * 0.5F))
                    val resultModel = MinecraftClient.getInstance().itemRenderer.getModel(recipe.icon(), null, null, 0)
                    MinecraftClient.getInstance().itemRenderer.render(recipe.icon(), ModelTransformationMode.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
            }

        }
    }
    fairyQuestCardScreenHandlerType.registerHandledScreen { gui, inventory, title -> FairyQuestCardScreen(gui, inventory, title) }
}
