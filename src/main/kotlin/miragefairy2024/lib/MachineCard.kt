package miragefairy2024.lib

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block

@Suppress("LeakingThis") // ブートストラップ問題のため解決不可能なので妥協する
abstract class MachineCard<B : Block, E : MachineBlockEntity<E>, H : MachineScreenHandler> {

    // Block

    abstract fun createBlockSettings(): FabricBlockSettings
    abstract fun createBlock(): B
    val block = createBlock()

}
