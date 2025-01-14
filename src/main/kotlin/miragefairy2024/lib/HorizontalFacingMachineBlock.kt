package miragefairy2024.lib

open class HorizontalFacingMachineBlock(private val card: MachineCard<*, *, *>) : SimpleHorizontalFacingBlock(card.createBlockSettings())
