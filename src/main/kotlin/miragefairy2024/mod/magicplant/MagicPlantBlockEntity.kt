package miragefairy2024.mod.magicplant

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

abstract class MagicPlantBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state) {

    private var traitStacks: TraitStacks? = null
    private var isRare = false

    fun getTraitStacks() = traitStacks

    fun setTraitStacks(traitStacks: TraitStacks) {
        this.traitStacks = traitStacks
        markDirty()
    }

    fun isRare() = isRare

    fun setRare(isRare: Boolean) {
        this.isRare = isRare
        markDirty()
    }

    override fun setWorld(world: World) {
        super.setWorld(world)
        if (traitStacks == null) {
            val block = world.getBlockState(pos).block
            val traitStackList = mutableListOf<TraitStack>()
            var isRare = false
            worldGenTraitGenerations.forEach {
                val result = it.spawn(world, pos, block)
                traitStackList += result.first
                if (result.second) isRare = true
            }
            setTraitStacks(TraitStacks.of(traitStackList))
            setRare(isRare)
        }
    }

    public override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        traitStacks = TraitStacks.readFromNbt(nbt)
        isRare = nbt.getBoolean("Rare")
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val nbt = super.toInitialChunkDataNbt()
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
        return nbt
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)

}

fun BlockView.getMagicPlantBlockEntity(blockPos: BlockPos) = this.getBlockEntity(blockPos) as? MagicPlantBlockEntity
