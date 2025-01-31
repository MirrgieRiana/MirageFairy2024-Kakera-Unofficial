package miragefairy2024.mod.fermentationbarrel

import miragefairy2024.ModContext

context(ModContext)
fun initFermentationBarrelModule() {
    FermentationBarrelCard.init()
    FermentationBarrelRecipe.init()
}
