package miragefairy2024.util

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

fun <T> T.register(registry: Registry<T>, identifier: Identifier) {
    Registry.register(registry, identifier, this)
}
