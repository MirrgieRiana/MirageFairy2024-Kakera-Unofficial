package miragefairy2024.lib

import net.minecraft.block.Block

open class MachineCard<C : MachineCard<C, B, E, H>, B : Block, E : MachineBlockEntity<E>, H : MachineScreenHandler>
