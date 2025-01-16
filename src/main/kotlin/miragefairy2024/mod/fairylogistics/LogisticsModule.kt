package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext

val fairyLogisticsNodeCards: List<FairyLogisticsCard<*, *, *>> = listOf(
    FairyMailboxCard,
    FairyDistributionCenterCard,
)

context(ModContext)
fun initFairyLogisticsModule() {
    fairyLogisticsNodeCards.forEach {
        it.init()
    }
}
