package miragefairy2024.client.mod.fairyhouse

import miragefairy2024.mod.fairyhouse.FairyCollectorCard
import miragefairy2024.mod.fairyhouse.FairyHouseCard
import miragefairy2024.mod.fairyhouse.FairyHouseModelCard
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.gui.screen.ingame.HandledScreens

fun initFairyHouseClientModule() {
    ModelLoadingPlugin.register {
        FairyHouseModelCard.entries.forEach { card ->
            it.addModels(card.identifier)
        }
    }

    HandledScreens.register(FairyHouseCard.screenHandlerType) { gui, inventory, title -> FairyHouseScreen(FairyFactoryHandledScreen.Arguments(gui, inventory, title)) }
    HandledScreens.register(FairyCollectorCard.screenHandlerType) { gui, inventory, title -> FairyCollectorScreen(FairyFactoryHandledScreen.Arguments(gui, inventory, title)) }
}
