package miragefairy2024.util

import com.google.gson.JsonElement
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.data.models.model.ModelTemplate as Model
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.data.models.model.TextureMapping as TextureMap
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation as Identifier
import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.Supplier


// Model Builder

fun Model(creator: (TextureMap) -> ModelData): Model = object : Model(Optional.empty(), Optional.empty()) {
    override fun upload(id: Identifier, textures: TextureMap, modelCollector: BiConsumer<Identifier, Supplier<JsonElement>>): Identifier {
        modelCollector.accept(id) { creator(textures).toJsonElement() }
        return id
    }
}

fun Model(parent: Identifier, vararg textureKeys: TextureKey) = Model(Optional.of(parent), Optional.empty(), *textureKeys)

fun Model(parent: Identifier, variant: String, vararg textureKeys: TextureKey) = Model(Optional.of(parent), Optional.of(variant), *textureKeys)

class ModelData(
    val parent: Identifier,
    val textures: ModelTexturesData? = null,
    val elements: ModelElementsData? = null,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "parent" to parent.string.jsonElement,
        "textures" to textures?.toJsonElement(),
        "elements" to elements?.toJsonElement(),
    )
}

class ModelTexturesData(val textures: List<Pair<String, String>>) {
    fun toJsonElement(): JsonElement = textures.map { it.first to it.second.jsonElement }.jsonObject
}

fun ModelTexturesData(vararg textures: Pair<String, String>) = ModelTexturesData(textures.toList())

class ModelElementsData(val elements: List<ModelElementData>) {
    fun toJsonElement(): JsonElement = elements.map { it.toJsonElement() }.jsonArray
}

fun ModelElementsData(vararg elements: ModelElementData) = ModelElementsData(elements.toList())

class ModelElementData(
    val from: List<Number>,
    val to: List<Number>,
    val faces: ModelFacesData,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "from" to from.map { it.jsonElement }.jsonArray,
        "to" to to.map { it.jsonElement }.jsonArray,
        "faces" to faces.toJsonElement(),
    )
}

class ModelFacesData(
    val down: ModelFaceData?,
    val up: ModelFaceData?,
    val north: ModelFaceData?,
    val south: ModelFaceData?,
    val west: ModelFaceData?,
    val east: ModelFaceData?,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "down" to down?.toJsonElement(),
        "up" to up?.toJsonElement(),
        "north" to north?.toJsonElement(),
        "south" to south?.toJsonElement(),
        "west" to west?.toJsonElement(),
        "east" to east?.toJsonElement(),
    )
}

class ModelFaceData(
    val uv: List<Number>? = null,
    val texture: String,
    val tintindex: Int? = null,
    val cullface: String? = null,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "uv" to uv?.map { it.jsonElement }?.jsonArray,
        "texture" to texture.jsonElement,
        "tintindex" to tintindex?.jsonElement,
        "cullface" to cullface?.jsonElement,
    )
}


// Util

fun TextureMap(vararg entries: Pair<TextureKey, Identifier>, initializer: TextureMap.() -> Unit = {}): TextureMap {
    val textureMap = TextureMap()
    entries.forEach {
        textureMap.put(it.first, it.second)
    }
    initializer(textureMap)
    return textureMap
}

val TextureKey.string get() = this.toString()

infix fun Model.with(textureMap: TextureMap): TexturedModel = TexturedModel.makeFactory({ textureMap }, this).get(Blocks.AIR)
fun Model.with(vararg textureEntries: Pair<TextureKey, Identifier>) = this with TextureMap(*textureEntries)


// registerModelGeneration

context(ModContext)
fun registerModelGeneration(identifierGetter: () -> Identifier, texturedModelCreator: () -> TexturedModel) = DataGenerationEvents.onGenerateBlockStateModel {
    val texturedModel = texturedModelCreator()
    texturedModel.model.upload(identifierGetter(), texturedModel.textures, it.modelCollector)
}

context(ModContext)
fun Item.registerModelGeneration(texturedModelCreator: () -> TexturedModel) = registerModelGeneration({ "item/" * this.getIdentifier() }) { texturedModelCreator() }

context(ModContext)
fun Item.registerModelGeneration(model: Model, textureMapCreator: () -> TextureMap = { TextureMap.layer0(this) }) = this.registerModelGeneration { model with textureMapCreator() }

context(ModContext)
fun Item.registerGeneratedModelGeneration() = this.registerModelGeneration(Models.GENERATED)

context(ModContext)
fun Item.registerBlockGeneratedModelGeneration(block: Block) = this.registerModelGeneration(Models.GENERATED) { TextureMap.layer0(block) }

context(ModContext)
fun Block.registerModelGeneration(texturedModelFactory: TexturedModel.Factory) = registerModelGeneration({ "block/" * this.getIdentifier() }) { texturedModelFactory.get(this) }
