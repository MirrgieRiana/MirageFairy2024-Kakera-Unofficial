package miragefairy2024.fabric

import miragefairy2024.ModEvents
import miragefairy2024.Modules
import net.fabricmc.api.ModInitializer

object MirageFairy2024FabricMod : ModInitializer {
    override fun onInitialize() {
        Modules.init()
        ModEvents.onRegistration.fire { it() }
        ModEvents.onInitialize.fire { it() }
    }
}
