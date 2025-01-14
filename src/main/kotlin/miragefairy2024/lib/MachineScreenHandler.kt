package miragefairy2024.lib

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.screen.ScreenHandler

abstract class MachineScreenHandler(screenHandlerType: ExtendedScreenHandlerType<out MachineScreenHandler>, syncId: Int) : ScreenHandler(screenHandlerType, syncId)
