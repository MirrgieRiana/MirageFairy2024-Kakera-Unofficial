package miragefairy2024.mod.machine

import miragefairy2024.ModContext

context(ModContext)
fun initMachineModule() {
    FermentationBarrelRecipeCard.init()
    AuraReflectorFurnaceRecipeCard.init()

    FermentationBarrelCard.init()
    AuraReflectorFurnaceCard.init()
}
