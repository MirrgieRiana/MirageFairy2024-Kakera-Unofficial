package miragefairy2024

import miragefairy2024.mod.fairy.initFairyModule
import miragefairy2024.mod.fairyquest.initFairyQuestModule
import miragefairy2024.mod.haimeviska.initHaimeviskaModule
import miragefairy2024.mod.initBiomeModule
import miragefairy2024.mod.initBlockMaterialsModule
import miragefairy2024.mod.initCommonModule
import miragefairy2024.mod.initExtraPlayerDataModule
import miragefairy2024.mod.initFoodIngredientsModule
import miragefairy2024.mod.initLastFoodModule
import miragefairy2024.mod.initMaterialsModule
import miragefairy2024.mod.initNinePatchTextureModule
import miragefairy2024.mod.initOresModule
import miragefairy2024.mod.initPoemModule
import miragefairy2024.mod.initRecipeGroupModule
import miragefairy2024.mod.initSoundEventModule
import miragefairy2024.mod.initStatusEffectModule
import miragefairy2024.mod.initVanillaModule
import miragefairy2024.mod.magicplant.initMagicPlantModule
import miragefairy2024.mod.passiveskill.initPassiveSkillModule
import miragefairy2024.mod.placeditem.initPlacedItemModule
import miragefairy2024.mod.rei.initReiModule
import miragefairy2024.mod.tool.initToolModule
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

class ModContext

class RegistrationContext

class InitializationContext

object ModEvents {
    val onRegistration = InitializationEventRegistry<context(RegistrationContext)() -> Unit>()
    val onInitialize = InitializationEventRegistry<context(InitializationContext)() -> Unit>()

    val onClientInit = InitializationEventRegistry<() -> Unit>()
}

object MirageFairy2024 : ModInitializer {
    val modId = "miragefairy2024"
    val logger = LoggerFactory.getLogger("miragefairy2024")

    var clientProxy: ClientProxy? = null

    override fun onInitialize() {
        Modules.init()
        ModEvents.onRegistration.fire { it(RegistrationContext()) }
        ModEvents.onInitialize.fire { it(InitializationContext()) }
    }
}

class InitializationEventRegistry<T> {
    private val list = mutableListOf<T>()
    private var closed = false

    context(ModContext)
    operator fun invoke(listener: T) {
        require(!closed)
        this.list += listener
    }

    fun fire(processor: (T) -> Unit) {
        closed = true
        this.list.forEach {
            processor(it)
        }
    }
}

object Modules {
    private val lock = Any()
    private var initialized = false
    fun init() {
        synchronized(lock) {
            if (initialized) return
            initialized = true

            with(ModContext()) {
                initCommonModule()
                initVanillaModule()
                initReiModule()
                initPoemModule()
                initStatusEffectModule()
                initMaterialsModule()
                initBlockMaterialsModule()
                initOresModule()
                initMagicPlantModule()
                initHaimeviskaModule()
                initFairyQuestModule()
                initNinePatchTextureModule()
                initPlacedItemModule()
                initFairyModule()
                initExtraPlayerDataModule()
                initPassiveSkillModule()
                initLastFoodModule()
                initFoodIngredientsModule()
                initRecipeGroupModule()
                initSoundEventModule()
                initToolModule()
                initBiomeModule()
            }
        }
    }
}
