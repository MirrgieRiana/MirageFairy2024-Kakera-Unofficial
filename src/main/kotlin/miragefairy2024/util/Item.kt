package miragefairy2024.util

import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.resources.ResourceLocation as Identifier

/** レジストリに登録する前に呼び出すことはできません。 */
fun Item.getIdentifier() = Registries.ITEM.getKey(this)

fun Identifier.toItem() = Registries.ITEM.get(this)
