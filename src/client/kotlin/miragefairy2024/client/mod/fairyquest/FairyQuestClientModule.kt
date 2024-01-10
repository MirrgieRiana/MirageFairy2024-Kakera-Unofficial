package miragefairy2024.client.mod.fairyquest

import miragefairy2024.mod.fairyquest.fairyQuestCardScreenHandlerType
import net.minecraft.client.gui.screen.ingame.HandledScreens

fun initFairyQuestClientModule() {
    HandledScreens.register(fairyQuestCardScreenHandlerType) { gui, inventory, title -> FairyQuestCardScreen(gui, inventory, title) }
}
