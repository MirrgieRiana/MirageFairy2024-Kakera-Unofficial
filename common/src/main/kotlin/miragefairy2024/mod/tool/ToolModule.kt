package miragefairy2024.mod.tool

import miragefairy2024.ModContext
import miragefairy2024.mod.tool.effects.initToolEffectType

context(ModContext)
fun initToolModule() {
    initDamageType()
    initToolMaterial()
    initToolCard()
    initToolConfiguration()
    initToolEffectType()
}
