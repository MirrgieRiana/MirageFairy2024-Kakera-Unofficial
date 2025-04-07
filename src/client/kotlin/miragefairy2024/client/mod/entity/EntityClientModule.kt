package miragefairy2024.client.mod.entity

import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.entity.EtheroballisticBoltCard
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPartData
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.util.Identifier

fun initEntityClientModule() {
    EntityModelLayerRegistry.registerModelLayer(AntimatterBoltEntityRenderer.MAIN.entityModelLayer, AntimatterBoltEntityRenderer.MAIN.provider)
    EntityRendererRegistry.register(AntimatterBoltCard.entityType, ::AntimatterBoltEntityRenderer)
    EntityModelLayerRegistry.registerModelLayer(ChaosCubeEntityRenderer.ROOT.entityModelLayer, ChaosCubeEntityRenderer.ROOT.provider)
    EntityRendererRegistry.register(ChaosCubeCard.entityType, ::ChaosCubeEntityRenderer)
    EntityModelLayerRegistry.registerModelLayer(EtheroballisticBoltEntityRenderer.MAIN.entityModelLayer, EtheroballisticBoltEntityRenderer.MAIN.provider)
    EntityRendererRegistry.register(EtheroballisticBoltCard.entityType, ::EtheroballisticBoltEntityRenderer)
}

class EntityModelLayerCard(
    identifier: Identifier,
    layerName: String,
    private val textureWidth: Int,
    private val textureHeight: Int,
    private val configurator: (ModelPartData) -> Unit,
) {
    val entityModelLayer = EntityModelLayer(identifier, layerName)
    val provider = EntityModelLayerRegistry.TexturedModelDataProvider {
        val modelData = ModelData()
        configurator(modelData.root)
        TexturedModelData.of(modelData, textureWidth, textureHeight)
    }
}
