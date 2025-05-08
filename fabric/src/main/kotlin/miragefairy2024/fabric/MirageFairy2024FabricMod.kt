package miragefairy2024.fabric

import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.util.Registration
import miragefairy2024.util.RegistryEvents
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry

object MirageFairy2024FabricMod : ModInitializer {
    override fun onInitialize() {
        Modules.init()

        RegistryEvents.registrations.forEach { registration ->
            fun <T : Any> f(registration: Registration<T>) {
                registration.holder = Registry.registerForHolder(registration.registry, registration.identifier, registration.creator())
            }
            f(registration)
        }

        ModEvents.onInitialize.fire { it() }
    }
}
