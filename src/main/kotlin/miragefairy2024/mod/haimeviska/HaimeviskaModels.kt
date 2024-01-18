package miragefairy2024.mod.haimeviska

import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.concat
import miragefairy2024.util.string
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.minecraft.data.client.TextureKey
import net.minecraft.util.Identifier

fun createHaimeviskaLeavesModel(identifier: Identifier) = Model {
    ModelData(
        parent = Identifier("minecraft", "block/block"),
        textures = ModelTexturesData(
            TextureKey.PARTICLE.name to TextureKey.BACK.string,
            TextureKey.BACK.name to ("block/" concat identifier).string,
            TextureKey.FRONT.name to ("block/" concat identifier concat "_blossom").string,
        ),
        elements = ModelElementsData(
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    down = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "down".jsonElement),
                    up = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "up".jsonElement),
                    north = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "north".jsonElement),
                    south = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "south".jsonElement),
                    west = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "west".jsonElement),
                    east = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "east".jsonElement),
                ),
            ),
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    down = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "down".jsonElement),
                    up = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "up".jsonElement),
                    north = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "north".jsonElement),
                    south = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "south".jsonElement),
                    west = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "west".jsonElement),
                    east = jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "east".jsonElement),
                ),
            ),
        ),
    )
}
