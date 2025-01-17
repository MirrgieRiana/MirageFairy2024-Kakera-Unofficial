package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext

val fairyLogisticsCards: List<FairyLogisticsCard<*, *, *>> = listOf(
    FairyPassiveSupplierCard,
    FairyActiveConsumerCard,
)

context(ModContext)
fun initFairyLogisticsModule() {
    fairyLogisticsCards.forEach {
        it.init()
    }
}
