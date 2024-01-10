package miragefairy2024.mod.fairyquest

import net.minecraft.client.gui.screen.ingame.HandledScreens

fun initFairyQuestClientModule() {
    HandledScreens.register(fairyQuestCardScreenHandlerType) { gui, inventory, title -> FairyQuestCardScreen(gui, inventory, title) }
}
