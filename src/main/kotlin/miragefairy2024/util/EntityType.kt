package miragefairy2024.util

import net.minecraft.world.entity.EntityType
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.resources.ResourceLocation

/** レジストリに登録する前に呼び出すことはできません。 */
fun EntityType<*>.getIdentifier() = Registries.ENTITY_TYPE.getKey(this)

fun ResourceLocation.toEntityType() = Registries.ENTITY_TYPE.get(this)
