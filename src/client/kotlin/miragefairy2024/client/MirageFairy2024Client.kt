package miragefairy2024.client

import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.client.mod.fairy.initFairyClientModule
import miragefairy2024.client.mod.fairyquest.initFairyQuestClientModule
import miragefairy2024.client.mod.initEntityClientModule
import miragefairy2024.client.mod.initExtraPlayerDataClientModule
import miragefairy2024.client.mod.initFairyBuildingClientModule
import miragefairy2024.client.mod.initFairyLogisticsClientModule
import miragefairy2024.client.mod.initFairyStatueClientModule
import miragefairy2024.client.mod.initMagicPlantClientModule
import miragefairy2024.client.mod.initParticleClientModule
import miragefairy2024.client.mod.initPlacedItemClientModule
import miragefairy2024.clientProxy
import net.fabricmc.api.ClientModInitializer

object MirageFairy2024Client : ClientModInitializer {
    override fun onInitializeClient() {
        Modules.init()
        clientProxy = ClientProxyImpl()
        ModEvents.onClientInit.fire { it() }

        initFairyQuestClientModule()
        initFairyClientModule()
        initExtraPlayerDataClientModule()
        initFairyBuildingClientModule()
        initMagicPlantClientModule()
        initEntityClientModule()
        initParticleClientModule()
        initFairyStatueClientModule()
        initPlacedItemClientModule()
        initFairyLogisticsClientModule()
    }
}
