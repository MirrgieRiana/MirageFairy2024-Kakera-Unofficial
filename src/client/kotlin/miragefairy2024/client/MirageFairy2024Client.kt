package miragefairy2024.client

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.client.mod.fairy.initFairyClientModule
import miragefairy2024.client.mod.fairyquest.initFairyQuestClientModule
import miragefairy2024.client.mod.initExtraPlayerDataClientModule
import net.fabricmc.api.ClientModInitializer

object MirageFairy2024Client : ClientModInitializer {
    override fun onInitializeClient() {
        Modules.init()
        MirageFairy2024.clientProxy = ClientProxyImpl()
        ModEvents.onClientInit.fire { it() }

        initFairyQuestClientModule()
        initFairyClientModule()
        initExtraPlayerDataClientModule()
    }
}
