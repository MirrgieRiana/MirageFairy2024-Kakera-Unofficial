package miragefairy2024.util

import com.google.gson.JsonElement
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import mirrg.kotlin.hydrogen.join
import mirrg.kotlin.hydrogen.or
import net.minecraft.block.Block
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.BlockStateSupplier
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

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

object VariantsBlockStateGenerationRegistrationScope

context(VariantsBlockStateGenerationRegistrationScope)
infix fun <T : Comparable<T>> List<Pair<List<PropertyEntry<*>>, BlockStateVariant>>.with(property: Property<T>): List<Pair<List<PropertyEntry<*>>, BlockStateVariant>> {
    return property.values.flatMap { value ->
        this.map { (properties, variant) ->
            val entry = property with value
            propertiesOf(*properties.toTypedArray(), entry) to variant.with(model = variant.getModel()!! * "_${entry.keyName}${entry.valueName}")
        }
    }
}

context(VariantsBlockStateGenerationRegistrationScope)
fun List<Pair<List<PropertyEntry<*>>, BlockStateVariant>>.withHorizontalRotation(property: Property<Direction>): List<Pair<List<PropertyEntry<*>>, BlockStateVariant>> {
    return property.values.flatMap { value ->
        this.map { (properties, variant) ->
            val entry = property with value
            val y = when (value) {
                Direction.NORTH -> BlockStateVariantRotation.R0
                Direction.EAST -> BlockStateVariantRotation.R90
                Direction.SOUTH -> BlockStateVariantRotation.R180
                Direction.WEST -> BlockStateVariantRotation.R270
                else -> BlockStateVariantRotation.R0
            }
            propertiesOf(*properties.toTypedArray(), entry) to variant.with(y = y)
        }
    }
}

context(VariantsBlockStateGenerationRegistrationScope)
fun normal(model: Identifier) = listOf(propertiesOf() to BlockStateVariant(model = model))

context(ModContext)
fun Block.registerVariantsBlockStateGeneration(entriesGetter: VariantsBlockStateGenerationRegistrationScope.() -> List<Pair<List<PropertyEntry<*>>, BlockStateVariant>>) = this.registerBlockStateGeneration {
    jsonObject(
        "variants" to jsonObject(
            *entriesGetter(VariantsBlockStateGenerationRegistrationScope)
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
