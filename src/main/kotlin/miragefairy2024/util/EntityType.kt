package miragefairy2024.util

import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/** レジストリに登録する前に呼び出すことはできません。 */
fun EntityType<*>.getIdentifier() = Registries.ENTITY_TYPE.getId(this)

fun Identifier.toEntityType() = Registries.ENTITY_TYPE.get(this)
