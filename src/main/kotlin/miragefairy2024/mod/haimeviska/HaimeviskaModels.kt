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
import net.minecraft.data.client.TextureKey
import net.minecraft.data.client.TexturedModel
import net.minecraft.util.Identifier

val haimeviskaLeavesTexturedModelFactory = TexturedModel.Factory { block ->
    Model { textureMap ->
        ModelData(
            parent = Identifier("minecraft", "block/block"),
            textures = ModelTexturesData(
                TextureKey.PARTICLE.name to TextureKey.BACK.string,
                TextureKey.BACK.name to textureMap.getTexture(TextureKey.BACK).string,
                TextureKey.FRONT.name to textureMap.getTexture(TextureKey.FRONT).string,
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
