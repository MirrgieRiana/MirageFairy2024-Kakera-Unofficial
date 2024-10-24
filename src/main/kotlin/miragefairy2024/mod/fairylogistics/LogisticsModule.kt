package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext

val fairyLogisticsNodeCards = listOf(
    FairyMailboxCard,
    FairyDistributionCenterCard,
)

context(ModContext)
fun initLogisticsModule() {
    fairyLogisticsNodeCards.forEach {
        it.configuration.init(it)
    }
}
