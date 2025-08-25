package miragefairy2024.client.mod.entity

import miragefairy2024.ModContext
import miragefairy2024.client.util.registerEntityRenderer
import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.entity.EtheroballisticBoltCard
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.model.geom.builders.PartDefinition
import net.minecraft.resources.ResourceLocation

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
    private val configurator: (PartDefinition) -> Unit,
) {
    val entityModelLayer = ModelLayerLocation(identifier, layerName)
    val provider = EntityModelLayerRegistry.TexturedModelDataProvider {
        val modelData = MeshDefinition()
        configurator(modelData.root)
        LayerDefinition.create(modelData, textureWidth, textureHeight)
    }
}
