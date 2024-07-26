package miragefairy2024.client

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.client.mod.fairy.initFairyClientModule
import miragefairy2024.client.mod.fairyquest.initFairyQuestClientModule
import miragefairy2024.client.mod.initExtraPlayerDataClientModule
import miragefairy2024.client.mod.initFairyHouseClientModule
import miragefairy2024.clientProxy
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.util.Identifier

object MirageFairy2024Client : ClientModInitializer {
    override fun onInitializeClient() {
        Modules.init()
        clientProxy = ClientProxyImpl()
        ModEvents.onClientInit.fire { it() }

        initFairyQuestClientModule()
        initFairyClientModule()
        initExtraPlayerDataClientModule()
        initFairyHouseClientModule()

        ModelLoadingPlugin.register {
            it.addModels(Identifier(MirageFairy2024.modId, "block/fairy_house/lantern"))
        }
    }
}
