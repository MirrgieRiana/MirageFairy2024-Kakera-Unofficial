package miragefairy2024.mod.haimeviska

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack

val haimeviskaAdvancement = AdvancementCard(
    identifier = MirageFairy2024.identifier("haimeviska"),
    context = AdvancementCard.Sub { rootAdvancement.await() },
    icon = { HaimeviskaBlockCard.DRIPPING_LOG.item().createItemStack() },
    name = EnJa("What is it like to be a plant?", "植物として生きるとはどのようなことか"),
    description = EnJa("Explore the overworld to find Haimeviska the fairy tree", "地上を探検して精樹ハイメヴィスカを探す"),
    criterion = AdvancementCard.hasItem { HaimeviskaBlockCard.LOG.item() },
)

context(ModContext)
fun initHaimeviskaModule() {
    initHaimeviskaBlocks()
    initHaimeviskaWorldGens()
    haimeviskaAdvancement.init()
}
