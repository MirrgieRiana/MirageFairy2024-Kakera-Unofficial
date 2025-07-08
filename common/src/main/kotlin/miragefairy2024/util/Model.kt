package miragefairy2024.util

import com.google.gson.JsonElement
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.Supplier
import net.minecraft.data.models.model.ModelTemplate as Model
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureMapping as TextureMap
import net.minecraft.data.models.model.TextureSlot as TextureKey


// Model Builder

fun Model(creator: (TextureMap) -> ModelData): Model = object : Model(Optional.empty(), Optional.empty()) {
    override fun create(id: ResourceLocation, textures: TextureMap, modelCollector: BiConsumer<ResourceLocation, Supplier<JsonElement>>): ResourceLocation {
        modelCollector.accept(id) { creator(textures).toJsonElement() }
        return id
    }
}

fun Model(parent: ResourceLocation, vararg textureKeys: TextureKey) = Model(Optional.of(parent), Optional.empty(), *textureKeys)

fun Model(parent: ResourceLocation, variant: String, vararg textureKeys: TextureKey) = Model(Optional.of(parent), Optional.of(variant), *textureKeys)

class ModelData(
    val parent: ResourceLocation,
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
    val down: ModelFaceData? = null,
    val up: ModelFaceData? = null,
    val north: ModelFaceData? = null,
    val south: ModelFaceData? = null,
    val west: ModelFaceData? = null,
    val east: ModelFaceData? = null,
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
    val rotation: Int? = null,
    val texture: String,
    val tintindex: Int? = null,
    val cullface: String? = null,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "uv" to uv?.map { it.jsonElement }?.jsonArray,
        "rotation" to rotation?.jsonElement,
        "texture" to texture.jsonElement,
        "tintindex" to tintindex?.jsonElement,
        "cullface" to cullface?.jsonElement,
    )
}


// Util

fun TextureMap(vararg entries: Pair<TextureKey, ResourceLocation>, initializer: TextureMap.() -> Unit = {}): TextureMap {
    val textureMap = TextureMap()
    entries.forEach {
        textureMap.put(it.first, it.second)
    }
    initializer(textureMap)
    return textureMap
}

val TextureKey.string get() = this.toString()

infix fun Model.with(textureMap: TextureMap): TexturedModel = TexturedModel.createDefault({ textureMap }, this).get(Blocks.AIR)
fun Model.with(vararg textureEntries: Pair<TextureKey, ResourceLocation>) = this with TextureMap(*textureEntries)


// registerModelGeneration

context(ModContext)
fun registerModelGeneration(identifierGetter: () -> ResourceLocation, texturedModelCreator: () -> TexturedModel) = DataGenerationEvents.onGenerateBlockModel {
    val texturedModel = texturedModelCreator()
    texturedModel.template.create(identifierGetter(), texturedModel.mapping, it.modelOutput)
}

context(ModContext)
@JvmName("registerItemModelGeneration")
fun (() -> Item).registerModelGeneration(texturedModelCreator: () -> TexturedModel) = registerModelGeneration({ "item/" * this().getIdentifier() }) { texturedModelCreator() }

context(ModContext)
@JvmName("registerItemModelGeneration")
fun (() -> Item).registerModelGeneration(model: Model, textureMapCreator: () -> TextureMap = { TextureMap.layer0(this()) }) = this.registerModelGeneration { model with textureMapCreator() }

context(ModContext)
@JvmName("registerItemGeneratedModelGeneration")
fun (() -> Item).registerGeneratedModelGeneration() = this.registerModelGeneration(Models.FLAT_ITEM)

context(ModContext)
@JvmName("registerItemBlockGeneratedModelGeneration")
fun (() -> Item).registerBlockGeneratedModelGeneration(block: () -> Block) = this.registerModelGeneration(Models.FLAT_ITEM) { TextureMap.layer0(block()) }

context(ModContext)
@JvmName("registerBlockModelGeneration")
fun (() -> Block).registerModelGeneration(texturedModelFactory: TexturedModel.Provider) = registerModelGeneration({ "block/" * this().getIdentifier() }) { texturedModelFactory.get(this()) }
