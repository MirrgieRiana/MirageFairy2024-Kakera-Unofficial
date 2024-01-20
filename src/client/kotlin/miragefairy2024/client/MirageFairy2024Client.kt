package miragefairy2024.client

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.fairy.initFairyClientModule
import miragefairy2024.client.mod.fairyquest.initFairyQuestClientModule
import net.fabricmc.api.ClientModInitializer

object MirageFairy2024Client : ClientModInitializer {
    override fun onInitializeClient() {
        val clientProxy = ClientProxyImpl()
        MirageFairy2024.clientProxy = clientProxy
        MirageFairy2024.onClientInit.fire { it(clientProxy) }

        initFairyQuestClientModule()
        initFairyClientModule()
    }
}
