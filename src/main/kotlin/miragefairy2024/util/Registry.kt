package miragefairy2024.util

import miragefairy2024.RegistrationContext
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

context(RegistrationContext)
fun <T> T.register(registry: Registry<T>, identifier: Identifier) {
    Registry.register(registry, identifier, this)
}
