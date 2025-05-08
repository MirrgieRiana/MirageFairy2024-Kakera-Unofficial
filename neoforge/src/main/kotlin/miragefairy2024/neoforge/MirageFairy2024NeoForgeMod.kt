package miragefairy2024.neoforge

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
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
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.BedBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
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
        ModEvents.onRegistration.fire { it() }
        ModEvents.onInitialize.fire { it() }
    }

    companion object {
        class Promise<T : Any> : () -> T {
            lateinit var value: T
            override fun invoke() = value
        }

        val block1 = Promise<Block>()
    }

    @SubscribeEvent
    fun register(event: RegisterEvent) {

        event.register(Registries.BLOCK) { registry ->
            val identifier = ResourceLocation.fromNamespaceAndPath(MirageFairy2024.MOD_ID, "block1")
            block1.value = BedBlock(DyeColor.RED, BlockBehaviour.Properties.of())
            registry.register(identifier, block1.value)
        }

        with(ModContext()) {
            ModEvents.onInitialize {

                val block = block1()

            }
        }

    }
}
