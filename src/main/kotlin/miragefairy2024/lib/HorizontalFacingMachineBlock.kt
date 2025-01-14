package miragefairy2024.lib

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings

open class HorizontalFacingMachineBlock(private val card: MachineCard<*, *, *>, settings: FabricBlockSettings) : SimpleHorizontalFacingBlock(settings)
