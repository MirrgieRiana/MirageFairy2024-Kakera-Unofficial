package miragefairy2024.util

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.block.Block

fun Block.registerFlammable(burn: Int, spread: Int) = FlammableBlockRegistry.getDefaultInstance().add(this, 30, 60)
