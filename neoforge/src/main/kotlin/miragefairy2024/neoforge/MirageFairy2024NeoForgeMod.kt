package miragefairy2024.neoforge

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import miragefairy2024.MirageFairy2024
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
import miragefairy2024.util.Registration
import miragefairy2024.util.RegistryEvents
import net.minecraft.core.Registry
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.registries.RegisterEvent

@Mod(MirageFairy2024.MOD_ID)
class MirageFairy2024NeoForgeMod {
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
        ModEvents.onInitialize.fire { it() }
    }

    @SubscribeEvent
    fun register(event: RegisterEvent) {
        RegistryEvents.registrations.forEach { registration ->
            fun <T : Any, U : T> f(registration: Registration<T, U>) {
                if (event.registry == registration.registry) {
                    val value = runBlocking {
                        val deferred = CompletableDeferred<U>()
                        launch(Dispatchers.Main.immediate) {
                            deferred.complete(registration.creator())
                        }
                        if (deferred.isActive) throw IllegalStateException("Illegal suspend for initialization: ${registration.identifier}")
                        deferred.await()
                    }
                    registration.complete(value, Registry.registerForHolder(registration.registry, registration.identifier, value))
                }
            }
            f(registration)
        }
    }
}
