package miragefairy2024.util

import net.minecraft.world.entity.EntityType
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.resources.ResourceLocation as Identifier

/** レジストリに登録する前に呼び出すことはできません。 */
fun EntityType<*>.getIdentifier() = Registries.ENTITY_TYPE.getId(this)

fun Identifier.toEntityType() = Registries.ENTITY_TYPE.get(this)
