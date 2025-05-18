package miragefairy2024.fabric

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.Modules
import terrablender.api.TerraBlenderApi

class MirageFairy2024FabricTerraBlenderEntryPoint : TerraBlenderApi {
    override fun onTerraBlenderInitialized() {
        with(ModContext()) {
            Modules.init()
        }
        ModEvents.onTerraBlenderInitialized.fire { it() }
    }
}
