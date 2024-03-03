package miragefairy2024.util

import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/** レジストリに登録する前に呼び出すことはできません。 */
fun Item.getIdentifier() = Registries.ITEM.getId(this)

fun Identifier.toItem() = Registries.ITEM.get(this)
