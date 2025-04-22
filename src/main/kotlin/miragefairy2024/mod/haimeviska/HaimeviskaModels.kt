package miragefairy2024.mod.haimeviska

import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.data.models.model.TextureSlot as TextureKey

val unchargedHaimeviskaLeavesTexturedModelFactory = TexturedModel.Provider { block ->
    Model { textureMap ->
        ModelData(
            parent = ResourceLocation.fromNamespaceAndPath("minecraft", "block/block"),
            textures = ModelTexturesData(
                TextureKey.PARTICLE.id to TextureKey.BACK.string,
                TextureKey.BACK.id to textureMap.get(TextureKey.BACK).string,
            ),
            elements = ModelElementsData(
                ModelElementData(
                    from = listOf(0, 0, 0),
                    to = listOf(16, 16, 16),
                    faces = ModelFacesData(
                        down = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "down"),
                        up = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "up"),
                        north = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "north"),
                        south = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "south"),
                        west = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "west"),
                        east = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "east"),
                    ),
                ),
            ),
        )
    }.with(
        TextureKey.BACK to "block/" * block.getIdentifier(),
    )
}

val chargedHaimeviskaLeavesTexturedModelFactory = TexturedModel.Provider { block ->
    Model { textureMap ->
        ModelData(
            parent = ResourceLocation.fromNamespaceAndPath("minecraft", "block/block"),
            textures = ModelTexturesData(
                TextureKey.PARTICLE.id to TextureKey.BACK.string,
                TextureKey.BACK.id to textureMap.get(TextureKey.BACK).string,
                TextureKey.FRONT.id to textureMap.get(TextureKey.FRONT).string,
            ),
            elements = ModelElementsData(
                ModelElementData(
                    from = listOf(0, 0, 0),
                    to = listOf(16, 16, 16),
                    faces = ModelFacesData(
                        down = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "down"),
                        up = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "up"),
                        north = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "north"),
                        south = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "south"),
                        west = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "west"),
                        east = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.BACK.string, tintindex = 0, cullface = "east"),
                    ),
                ),
                ModelElementData(
                    from = listOf(0, 0, 0),
                    to = listOf(16, 16, 16),
                    faces = ModelFacesData(
                        down = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.FRONT.string, cullface = "down"),
                        up = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.FRONT.string, cullface = "up"),
                        north = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.FRONT.string, cullface = "north"),
                        south = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.FRONT.string, cullface = "south"),
                        west = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.FRONT.string, cullface = "west"),
                        east = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureKey.FRONT.string, cullface = "east"),
                    ),
                ),
            ),
        )
    }.with(
        TextureKey.BACK to "block/" * block.getIdentifier(),
        TextureKey.FRONT to "block/" * block.getIdentifier() * "_blossom",
    )
}
