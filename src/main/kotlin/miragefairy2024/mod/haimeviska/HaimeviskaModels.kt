package miragefairy2024.mod.haimeviska

import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementsData
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
            jsonObject(
                "from" to jsonArray(0.jsonElement, 0.jsonElement, 0.jsonElement),
                "to" to jsonArray(16.jsonElement, 16.jsonElement, 16.jsonElement),
                "faces" to jsonObject(
                    "down" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "down".jsonElement),
                    "up" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "up".jsonElement),
                    "north" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "north".jsonElement),
                    "south" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "south".jsonElement),
                    "west" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "west".jsonElement),
                    "east" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.BACK.string.jsonElement, "tintindex" to 0.jsonElement, "cullface" to "east".jsonElement),
                ),
            ),
            jsonObject(
                "from" to jsonArray(0.jsonElement, 0.jsonElement, 0.jsonElement),
                "to" to jsonArray(16.jsonElement, 16.jsonElement, 16.jsonElement),
                "faces" to jsonObject(
                    "down" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "down".jsonElement),
                    "up" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "up".jsonElement),
                    "north" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "north".jsonElement),
                    "south" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "south".jsonElement),
                    "west" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "west".jsonElement),
                    "east" to jsonObject("uv" to jsonArray(0.jsonElement, 0.jsonElement, 16.jsonElement, 16.jsonElement), "texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "east".jsonElement),
                ),
            ),
        ),
    )
}
