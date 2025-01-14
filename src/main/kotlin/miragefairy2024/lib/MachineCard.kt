package miragefairy2024.lib

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block

abstract class MachineCard<B : Block, E : MachineBlockEntity<E>, H : MachineScreenHandler> {

    abstract fun createBlockSettings(): FabricBlockSettings

}
