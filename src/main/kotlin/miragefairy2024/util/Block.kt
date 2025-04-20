package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.resources.ResourceLocation

/** レジストリに登録する前に呼び出すことはできません。 */
fun Block.getIdentifier() = BuiltInRegistries.BLOCK.getKey(this)

fun ResourceLocation.toBlock() = BuiltInRegistries.BLOCK.get(this)

context(ModContext)
fun Block.registerFlammable(burn: Int, spread: Int) = ModEvents.onInitialize {
    FlammableBlockRegistry.getDefaultInstance().add(this, 30, 60)
}

fun <T : Comparable<T>> BlockState.getOrNull(property: Property<T>): T? {
    val value = this.values[property] ?: return null
    return property.valueClass.cast(value)
}

fun <T : Comparable<T>> BlockState.getOr(property: Property<T>, default: () -> T) = this.getOrNull(property) ?: default()
