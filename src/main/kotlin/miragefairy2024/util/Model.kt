package miragefairy2024.util

import com.google.gson.JsonElement
import miragefairy2024.MirageFairy2024DataGenerator
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
import net.minecraft.data.client.ModelIds
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
    val textures: JsonElement? = null,
    val elements: JsonElement? = null,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "parent" to parent.string.jsonElement,
        "textures" to textures,
        "elements" to elements,
    )
}


fun TextureMap(vararg entries: Pair<TextureKey, Identifier>, initializer: TextureMap.() -> Unit = {}): TextureMap {
    val textureMap = TextureMap()
    entries.forEach {
        textureMap.put(it.first, it.second)
    }
    initializer(textureMap)
    return textureMap
}

val TextureKey.string get() = this.toString()


fun Model.with(vararg textureEntries: Pair<TextureKey, Identifier>): TexturedModel = TexturedModel.makeFactory({ TextureMap(*textureEntries) }, this).get(Blocks.AIR)


fun Item.registerItemModelGeneration(model: Model) = MirageFairy2024DataGenerator.itemModelGenerators {
    it.register(this, model)
}

fun Item.registerGeneratedItemModelGeneration() = this.registerItemModelGeneration(Models.GENERATED)

fun Item.registerBlockItemModelGeneration(block: Block) = MirageFairy2024DataGenerator.itemModelGenerators {
    Models.GENERATED.upload(ModelIds.getItemModelId(this), TextureMap.layer0(block), it.writer)
}


fun TexturedModel.registerModelGeneration(identifier: Identifier) = MirageFairy2024DataGenerator.blockStateModelGenerators {
    this.model.upload(identifier, this.textures, it.modelCollector)
}

fun Model.registerModelGeneration(identifier: Identifier, vararg textureEntries: Pair<TextureKey, Identifier>) = this.with(*textureEntries).registerModelGeneration(identifier)

fun Block.registerModelGeneration(texturedModel: TexturedModel) = MirageFairy2024DataGenerator.blockStateModelGenerators {
    texturedModel.model.upload("block/" concat this.getIdentifier(), texturedModel.textures, it.modelCollector)
}

fun Block.registerModelGeneration(factory: TexturedModel.Factory) = this.registerModelGeneration(factory.get(this))


fun Block.registerBlockStateGeneration(creator: () -> JsonElement) = MirageFairy2024DataGenerator.blockStateModelGenerators {
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
                propertiesOf(*properties.toTypedArray(), entry) to variant.with(model = variant.getModel()!! concat "_${entry.keyName}${entry.valueName}")
            }
        }
    }

    fun normal(model: Identifier) = listOf(propertiesOf() to BlockStateVariant(model = model))
}

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

fun Block.registerSingletonBlockStateGeneration() = MirageFairy2024DataGenerator.blockStateModelGenerators {
    it.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(this, "block/" concat this.getIdentifier()))
}
