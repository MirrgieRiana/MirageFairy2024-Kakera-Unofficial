package miragefairy2024.client.mod.entity

import miragefairy2024.ModContext
import miragefairy2024.client.util.registerEntityRenderer
import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.entity.EtheroballisticBoltCard
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.client.model.geom.ModelLayerLocation as EntityModelLayer
import net.minecraft.client.model.geom.builders.LayerDefinition as TexturedModelData
import net.minecraft.client.model.geom.builders.MeshDefinition as ModelData
import net.minecraft.client.model.geom.builders.PartDefinition as ModelPartData

context(ModContext)
fun initEntityClientModule() {
    EntityModelLayerRegistry.registerModelLayer(AntimatterBoltEntityRenderer.MAIN.entityModelLayer, AntimatterBoltEntityRenderer.MAIN.provider)
    AntimatterBoltCard.entityType.registerEntityRenderer(::AntimatterBoltEntityRenderer)
    EntityModelLayerRegistry.registerModelLayer(ChaosCubeEntityRenderer.ROOT.entityModelLayer, ChaosCubeEntityRenderer.ROOT.provider)
    ChaosCubeCard.entityType.registerEntityRenderer(::ChaosCubeEntityRenderer)
    EntityModelLayerRegistry.registerModelLayer(EtheroballisticBoltEntityRenderer.MAIN.entityModelLayer, EtheroballisticBoltEntityRenderer.MAIN.provider)
    EtheroballisticBoltCard.entityType.registerEntityRenderer(::EtheroballisticBoltEntityRenderer)
}

class EntityModelLayerCard(
    identifier: ResourceLocation,
    layerName: String,
    private val textureWidth: Int,
    private val textureHeight: Int,
    private val configurator: (ModelPartData) -> Unit,
) {
    val entityModelLayer = EntityModelLayer(identifier, layerName)
    val provider = EntityModelLayerRegistry.TexturedModelDataProvider {
        val modelData = ModelData()
        configurator(modelData.root)
        TexturedModelData.create(modelData, textureWidth, textureHeight)
    }
}
