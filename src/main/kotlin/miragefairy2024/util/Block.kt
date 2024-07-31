package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier

/** レジストリに登録する前に呼び出すことはできません。 */
fun Block.getIdentifier() = Registries.BLOCK.getId(this)

fun Identifier.toBlock() = Registries.BLOCK.get(this)

context(ModContext)
fun Block.registerFlammable(burn: Int, spread: Int) = ModEvents.onInitialize {
    FlammableBlockRegistry.getDefaultInstance().add(this, 30, 60)
}

fun <T : Comparable<T>> BlockState.getOrNull(property: Property<T>): T? {
    val value = this.entries[property] ?: return null
    return property.type.cast(value)
}

fun <T : Comparable<T>> BlockState.getOr(property: Property<T>, default: () -> T) = this.getOrNull(property) ?: default()
