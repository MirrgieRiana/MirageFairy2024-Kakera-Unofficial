package miragefairy2024

import miragefairy2024.mod.entity.initEntityModule
import miragefairy2024.mod.fairy.initFairyModule
import miragefairy2024.mod.fairybuilding.initFairyBuildingModule
import miragefairy2024.mod.fairylogistics.initFairyLogisticsModule
import miragefairy2024.mod.fairyquest.initFairyQuestModule
import miragefairy2024.mod.haimeviska.initHaimeviskaModule
import miragefairy2024.mod.initBagModule
import miragefairy2024.mod.initBiomeModule
import miragefairy2024.mod.initBlockMaterialsModule
import miragefairy2024.mod.initCommonModule
import miragefairy2024.mod.initDebrisModule
import miragefairy2024.mod.initEnchantmentModule
import miragefairy2024.mod.initExtraPlayerDataModule
import miragefairy2024.mod.initFairyFountainModule
import miragefairy2024.mod.initFoodIngredientsModule
import miragefairy2024.mod.initLastFoodModule
import miragefairy2024.mod.initMaterialsModule
import miragefairy2024.mod.initNinePatchTextureModule
import miragefairy2024.mod.initOresModule
import miragefairy2024.mod.initParticleModule
import miragefairy2024.mod.initPoemModule
import miragefairy2024.mod.initRecipeGroupModule
import miragefairy2024.mod.initSoundEventModule
import miragefairy2024.mod.initStatusEffectModule
import miragefairy2024.mod.initStructureModule
import miragefairy2024.mod.initTelescopeModule
import miragefairy2024.mod.initVanillaModule
import miragefairy2024.mod.machine.initMachineModule
import miragefairy2024.mod.magicplant.initMagicPlantModule
import miragefairy2024.mod.passiveskill.initPassiveSkillModule
import miragefairy2024.mod.placeditem.initPlacedItemModule
import miragefairy2024.mod.rei.initReiModule
import miragefairy2024.mod.tool.initToolModule

context(ModContext)
fun initModules() {
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
    initFairyBuildingModule()
    initEntityModule()
    initParticleModule()
    initEnchantmentModule()
    initTelescopeModule()
    initFairyFountainModule()
    initFairyLogisticsModule()
    initDebrisModule()
    initBagModule()
    initMachineModule()
    initStructureModule()
}
