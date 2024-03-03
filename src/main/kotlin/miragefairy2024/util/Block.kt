package miragefairy2024.util

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/** レジストリに登録する前に呼び出すことはできません。 */
fun Block.getIdentifier() = Registries.BLOCK.getId(this)

fun Identifier.toBlock() = Registries.BLOCK.get(this)

fun Block.registerFlammable(burn: Int, spread: Int) = FlammableBlockRegistry.getDefaultInstance().add(this, 30, 60)
