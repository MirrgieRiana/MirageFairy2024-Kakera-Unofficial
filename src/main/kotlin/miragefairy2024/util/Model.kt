package miragefairy2024.util

import com.google.gson.JsonElement
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import mirrg.kotlin.hydrogen.join
import mirrg.kotlin.hydrogen.or
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.BlockStateSupplier
import net.minecraft.data.client.Model
import net.minecraft.data.client.Models
import net.minecraft.data.client.TextureKey
import net.minecraft.data.client.TextureMap
import net.minecraft.data.client.TexturedModel
import net.minecraft.item.Item
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
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


// registerBlockStateGeneration

context(ModContext)
fun Block.registerBlockStateGeneration(creator: () -> JsonElement) = DataGenerationEvents.onGenerateBlockStateModel {
    it.blockStateCollector.accept(object : BlockStateSupplier {
        override fun get() = creator()
        override fun getBlock() = this@registerBlockStateGeneration
    })
}

enum class BlockStateVariantRotation(val degrees: Int) {
    R0(0),
    R90(90),
    R180(180),
    R270(270),
}

class BlockStateVariant(
    private val parent: BlockStateVariant? = null,
    private val model: Identifier? = null,
    private val x: BlockStateVariantRotation? = null,
    private val y: BlockStateVariantRotation? = null,
    private val uvlock: Boolean? = null,
    private val weight: Int? = null,
) {
    fun getModel() = model.or { parent?.model }
    fun getX() = x.or { parent?.x }
    fun getY() = y.or { parent?.y }
    fun getUvlock() = uvlock.or { parent?.uvlock }
    fun getWeight() = weight.or { parent?.weight }
}

fun BlockStateVariant.with(
    model: Identifier? = null,
    x: BlockStateVariantRotation? = null,
    y: BlockStateVariantRotation? = null,
    uvlock: Boolean? = null,
    weight: Int? = null,
) = BlockStateVariant(
    parent = this,
    model = model,
    x = x,
    y = y,
    uvlock = uvlock,
    weight = weight,
)

fun BlockStateVariant.toJson(): JsonElement = jsonObjectNotNull(
    getModel()?.let { "model" to it.string.jsonElement },
    getX()?.let { "x" to it.degrees.jsonElement },
    getY()?.let { "y" to it.degrees.jsonElement },
    getUvlock()?.let { "uvlock" to it.jsonElement },
    getWeight()?.let { "weight" to it.jsonElement },
)

class PropertyEntry<T : Comparable<T>>(val key: Property<T>, val value: T)

val PropertyEntry<*>.keyName: String get() = this.key.name
val <T : Comparable<T>> PropertyEntry<T>.valueName: String get() = this.key.name(this.value)

infix fun <T : Comparable<T>> Property<T>.with(value: T) = PropertyEntry(this, value)

fun propertiesOf(vararg properties: PropertyEntry<*>) = listOf(*properties)

class VariantsBlockStateGenerationRegistrationScope {
    infix fun <T : Comparable<T>> List<Pair<List<PropertyEntry<*>>, BlockStateVariant>>.with(property: Property<T>): List<Pair<List<PropertyEntry<*>>, BlockStateVariant>> {
        return property.values.flatMap { value ->
            this.map { (properties, variant) ->
                val entry = property with value
                propertiesOf(*properties.toTypedArray(), entry) to variant.with(model = variant.getModel()!! * "_${entry.keyName}${entry.valueName}")
            }
        }
    }

    fun normal(model: Identifier) = listOf(propertiesOf() to BlockStateVariant(model = model))
}

context(ModContext)
fun Block.registerVariantsBlockStateGeneration(entriesGetter: VariantsBlockStateGenerationRegistrationScope.() -> List<Pair<List<PropertyEntry<*>>, BlockStateVariant>>) = this.registerBlockStateGeneration {
    jsonObject(
        "variants" to jsonObject(
            *entriesGetter(VariantsBlockStateGenerationRegistrationScope())
                .map { (propertiesMap, modelId) ->
                    val propertiesString = propertiesMap
                        .sortedBy { it.keyName }
                        .map { "${it.keyName}=${it.valueName}" }
                        .join(",")
                    propertiesString to modelId
                }
                .sortedBy { (propertiesString, _) -> propertiesString }
                .map { (propertiesString, value) -> propertiesString to value.toJson() }
                .toTypedArray()
        )
    )
}

context(ModContext)
fun Block.registerSingletonBlockStateGeneration() = DataGenerationEvents.onGenerateBlockStateModel {
    it.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(this, "block/" * this.getIdentifier()))
}
