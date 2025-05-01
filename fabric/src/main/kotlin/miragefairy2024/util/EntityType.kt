package miragefairy2024.util

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType

/** レジストリに登録する前に呼び出すことはできません。 */
fun EntityType<*>.getIdentifier() = BuiltInRegistries.ENTITY_TYPE.getKey(this)

fun ResourceLocation.toEntityType() = BuiltInRegistries.ENTITY_TYPE.get(this)
