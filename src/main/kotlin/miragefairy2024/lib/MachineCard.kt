package miragefairy2024.lib

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.util.Identifier

@Suppress("LeakingThis") // ブートストラップ問題のため解決不可能なので妥協する
abstract class MachineCard<B : Block, E : MachineBlockEntity<E>, H : MachineScreenHandler> {

    // Specification

    abstract fun createIdentifier(): Identifier
    val identifier = createIdentifier()


    // Block

    abstract fun createBlockSettings(): FabricBlockSettings
    abstract fun createBlock(): B
    val block = createBlock()

}
