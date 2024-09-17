package miragefairy2024

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier

object ModEvents {
    val onRegistration = InitializationEventRegistry<() -> Unit>()
    val onInitialize = InitializationEventRegistry<() -> Unit>()

    val onClientInit = InitializationEventRegistry<() -> Unit>()
}

object MirageFairy2024 : ModInitializer {
    const val MOD_ID = "miragefairy2024"
    fun identifier(path: String) = Identifier(MOD_ID, path)
    override fun onInitialize() {
        Modules.init()
        ModEvents.onRegistration.fire { it() }
        ModEvents.onInitialize.fire { it() }
    }
}
