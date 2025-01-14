package miragefairy2024.client.lib

import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

abstract class MachineScreen<H : MachineScreenHandler>(private val card: MachineCard<*, *, *>, handler: H, playerInventory: PlayerInventory, title: Text) : HandledScreen<H>(handler, playerInventory, title)
