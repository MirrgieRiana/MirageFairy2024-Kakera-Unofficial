package miragefairy2024.neoforge

import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.client.ClientProxyImpl
import miragefairy2024.client.mod.entity.initEntityClientModule
import miragefairy2024.client.mod.fairy.initFairyClientModule
import miragefairy2024.client.mod.fairyquest.initFairyQuestClientModule
import miragefairy2024.client.mod.initBagClientModule
import miragefairy2024.client.mod.initExtraPlayerDataClientModule
import miragefairy2024.client.mod.initFairyBuildingClientModule
import miragefairy2024.client.mod.initFairyLogisticsClientModule
import miragefairy2024.client.mod.initFairyStatueClientModule
import miragefairy2024.client.mod.initMachineClientModule
import miragefairy2024.client.mod.initMagicPlantClientModule
import miragefairy2024.client.mod.initPlacedItemClientModule
import miragefairy2024.client.mod.initSoundEventClientModule
import miragefairy2024.client.mod.particle.initParticleClientModule
import miragefairy2024.clientProxy
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment

@Mod(MirageFairy2024Mod.MOD_ID)
class MirageFairy2024Mod {
    companion object {
        const val MOD_ID = "miragefairy2024"
    }

    init {
        if (FMLEnvironment.dist == Dist.CLIENT) {
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
            initBagClientModule()
            initMachineClientModule()
            initSoundEventClientModule()
        }

        Modules.init()
        ModEvents.onRegistration.fire { it() }
        ModEvents.onInitialize.fire { it() }
    }
}
