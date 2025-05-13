package miragefairy2024.client.fabric

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.client.ClientProxyImpl
import miragefairy2024.client.initClientModules
import miragefairy2024.clientProxy
import net.fabricmc.api.ClientModInitializer

object MirageFairy2024FabricClientMod : ClientModInitializer {
    override fun onInitializeClient() {
        with(ModContext()) {
            initClientModules()
        }

        clientProxy = ClientProxyImpl()
        ModEvents.onClientInit.fire { it() }
    }
}
