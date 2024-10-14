package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext

val fairyLogisticsBlockCards = listOf(
    FairyMailboxCard,
    FairyDistributionCenterCard,
)

context(ModContext)
fun initLogisticsModule() {
    fairyLogisticsBlockCards.forEach {
        it.configuration.init(it)
    }
}
