package miragefairy2024.client.mod.fairy

import miragefairy2024.mod.fairy.motifTableScreenHandlerType
import net.minecraft.client.gui.screen.ingame.HandledScreens

fun initFairyClientModule() {
    HandledScreens.register(motifTableScreenHandlerType) { gui, inventory, title -> MotifTableScreen(gui, inventory, title) }
}
