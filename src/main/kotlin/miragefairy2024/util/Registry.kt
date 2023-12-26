package miragefairy2024.util

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


fun <T> T.register(registry: Registry<T>, identifier: Identifier) {
    Registry.register(registry, identifier, this)
}


/** レジストリに登録する前に呼び出すことはできません。 */
fun Block.getIdentifier() = Registries.BLOCK.getId(this)

fun Identifier.toBlock() = Registries.BLOCK.get(this)


/** レジストリに登録する前に呼び出すことはできません。 */
fun Item.getIdentifier() = Registries.ITEM.getId(this)

fun Identifier.toItem() = Registries.ITEM.get(this)
