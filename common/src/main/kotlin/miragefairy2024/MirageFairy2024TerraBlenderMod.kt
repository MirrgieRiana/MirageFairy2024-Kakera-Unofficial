package miragefairy2024

import terrablender.api.TerraBlenderApi

object TerraBlenderEvents {
    val onTerraBlenderInitialized = InitializationEventRegistry<() -> Unit>()
}

class MirageFairy2024TerraBlenderMod : TerraBlenderApi {
    override fun onTerraBlenderInitialized() {
        with(ModContext()) {
            Modules.init()
        }
        TerraBlenderEvents.onTerraBlenderInitialized.fire { it() }
    }
}
