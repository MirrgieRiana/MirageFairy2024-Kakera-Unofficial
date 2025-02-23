package miragefairy2024.mod.entity

import miragefairy2024.ModContext
import miragefairy2024.util.register
import net.minecraft.registry.Registries

context(ModContext)
fun initEntityModule() {
    AntimatterBoltCard.let { card ->
        card.entityType.register(Registries.ENTITY_TYPE, card.identifier)
        card.init()
    }
}
