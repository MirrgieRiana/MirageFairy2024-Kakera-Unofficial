package miragefairy2024.mod.magicplant

import mirrg.kotlin.hydrogen.or
import net.minecraft.block.Block
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

class MagicPlantBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state) {

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
            val result = spawnTraitStacks(world, pos, block)
            setTraitStacks(result.first)
            setRare(result.second)
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

fun spawnTraitStacks(world: World, blockPos: BlockPos, block: Block): Pair<TraitStacks, Boolean> {
    val resultTraitStackList = mutableListOf<TraitStack>()
    var isRare = false

    // レシピ判定
    val aTraitStackList = mutableListOf<TraitStack>()
    val cTraitStackList = mutableListOf<TraitStack>()
    val nTraitStackList = mutableListOf<TraitStack>()
    val rTraitStackList = mutableListOf<TraitStack>()
    val sTraitStackList = mutableListOf<TraitStack>()
    worldGenTraitRecipeRegistry[block].or { listOf() }.forEach { recipe ->
        if (recipe.condition.canSpawn(world, blockPos)) {
            val traitStackList = when (recipe.rarity) {
                TraitSpawnRarity.A -> aTraitStackList
                TraitSpawnRarity.C -> cTraitStackList
                TraitSpawnRarity.N -> nTraitStackList
                TraitSpawnRarity.R -> rTraitStackList
                TraitSpawnRarity.S -> sTraitStackList
            }
            traitStackList += TraitStack(recipe.trait, recipe.level)
        }
    }

    // 抽選
    val r = world.random.nextDouble()
    when {
        r < 0.01 -> { // +S
            resultTraitStackList += aTraitStackList
            resultTraitStackList += cTraitStackList
            if (sTraitStackList.isNotEmpty()) {
                resultTraitStackList += sTraitStackList[world.random.nextInt(sTraitStackList.size)]
                isRare = true
            }
        }

        r >= 0.02 && r < 0.1 -> { // +R
            resultTraitStackList += aTraitStackList
            resultTraitStackList += cTraitStackList
            if (rTraitStackList.isNotEmpty()) {
                resultTraitStackList += rTraitStackList[world.random.nextInt(rTraitStackList.size)]
            }
        }

        r >= 0.01 && r < 0.02 -> { // -C
            resultTraitStackList += aTraitStackList
            if (cTraitStackList.isNotEmpty()) {
                cTraitStackList.removeAt(world.random.nextInt(cTraitStackList.size))
                resultTraitStackList += cTraitStackList
                isRare = true
            }
        }

        else -> { // +N
            resultTraitStackList += aTraitStackList
            resultTraitStackList += cTraitStackList
            if (nTraitStackList.isNotEmpty()) {
                resultTraitStackList += nTraitStackList[world.random.nextInt(nTraitStackList.size)]
            }
        }
    }

    return Pair(TraitStacks.of(resultTraitStackList), isRare)
}
