package miragefairy2024

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier

object ModEvents {
    val onRegistration = InitializationEventRegistry<() -> Unit>()
    val onInitialize = InitializationEventRegistry<() -> Unit>()

    val onClientInit = InitializationEventRegistry<() -> Unit>()
}

object MirageFairy2024 : ModInitializer {
    val modId = "miragefairy2024"
    fun identifier(path: String) = Identifier(modId, path)
    override fun onInitialize() {
        Modules.init()
        ModEvents.onRegistration.fire { it() }
        ModEvents.onInitialize.fire { it() }
    }
}
