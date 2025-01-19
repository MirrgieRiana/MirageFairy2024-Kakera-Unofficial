package miragefairy2024.client.mod

import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.mod.fairylogistics.FairyActiveConsumerCard
import miragefairy2024.mod.fairylogistics.FairyActiveConsumerScreenHandler
import miragefairy2024.mod.fairylogistics.FairyLogisticsCard
import miragefairy2024.mod.fairylogistics.FairyLogisticsScreenHandler
import miragefairy2024.mod.fairylogistics.FairyPassiveSupplierCard
import miragefairy2024.mod.fairylogistics.FairyPassiveSupplierScreenHandler
import net.minecraft.client.gui.screen.ingame.HandledScreens

fun initFairyLogisticsClientModule() {
    HandledScreens.register(FairyPassiveSupplierCard.screenHandlerType) { gui, inventory, title -> FairyPassiveSupplierScreen(FairyPassiveSupplierCard, MachineScreen.Arguments(gui, inventory, title)) }
    HandledScreens.register(FairyActiveConsumerCard.screenHandlerType) { gui, inventory, title -> FairyActiveConsumerScreen(FairyActiveConsumerCard, MachineScreen.Arguments(gui, inventory, title)) }
}

open class FairyLogisticsScreen<H : FairyLogisticsScreenHandler>(card: FairyLogisticsCard<*, *, *>, arguments: Arguments<H>) : MachineScreen<H>(card, arguments)

class FairyPassiveSupplierScreen(card: FairyPassiveSupplierCard, arguments: Arguments<FairyPassiveSupplierScreenHandler>) : FairyLogisticsScreen<FairyPassiveSupplierScreenHandler>(card, arguments)

class FairyActiveConsumerScreen(card: FairyActiveConsumerCard, arguments: Arguments<FairyActiveConsumerScreenHandler>) : FairyLogisticsScreen<FairyActiveConsumerScreenHandler>(card, arguments)
