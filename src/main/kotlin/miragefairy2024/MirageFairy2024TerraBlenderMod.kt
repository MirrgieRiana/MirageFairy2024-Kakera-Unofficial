package miragefairy2024

import terrablender.api.TerraBlenderApi

object TerraBlenderEvents {
    val onTerraBlenderInitialized = InitializationEventRegistry<ModContext, () -> Unit>()
}

class MirageFairy2024TerraBlenderMod : TerraBlenderApi {
    override fun onTerraBlenderInitialized() {
        Modules.init()
        TerraBlenderEvents.onTerraBlenderInitialized.fire { it() }
    }
}
