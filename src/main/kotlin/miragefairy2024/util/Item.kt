package miragefairy2024.util

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

/** レジストリに登録する前に呼び出すことはできません。 */
fun Item.getIdentifier() = BuiltInRegistries.ITEM.getKey(this)

fun ResourceLocation.toItem() = BuiltInRegistries.ITEM.get(this)
