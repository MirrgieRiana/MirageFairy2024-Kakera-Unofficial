package miragefairy2024.mod.magicplant

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.biome.Biome

class MagicPlantBlockEntity(private val configuration: MagicPlantConfiguration<*, *>, pos: BlockPos, state: BlockState) : BlockEntity(configuration.card.blockEntityType, pos, state) {

    private var traitStacks: TraitStacks? = null

    fun getTraitStacks() = traitStacks

    fun setTraitStacks(traitStacks: TraitStacks) {
        this.traitStacks = traitStacks
        markDirty()
    }


    private var isRare = false

    fun isRare() = isRare

    fun setRare(isRare: Boolean) {
        this.isRare = isRare
        markDirty()
    }


    private var isNatural = false

    fun isNatural() = isNatural

    fun setNatural(isNatural: Boolean) {
        this.isNatural = isNatural
        markDirty()
    }


    override fun setWorld(world: World) {
        super.setWorld(world)
        if (traitStacks == null) {
            val result = spawnTraitStacks(configuration.possibleTraits, world.getBiome(pos), world.random)
            setTraitStacks(result.first)
            setRare(result.second)
            setNatural(true)
        }
    }

    public override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
        if (isNatural) nbt.putBoolean("Natural", false)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        traitStacks = TraitStacks.readFromNbt(nbt)
        isRare = nbt.getBoolean("Rare")
        isNatural = nbt.getBoolean("Natural")
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val nbt = super.toInitialChunkDataNbt()
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
        if (isNatural) nbt.putBoolean("Natural", true)
        return nbt
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)

}

fun BlockView.getMagicPlantBlockEntity(blockPos: BlockPos) = this.getBlockEntity(blockPos) as? MagicPlantBlockEntity

private fun spawnTraitStacks(possibleTraits: Iterable<Trait>, biome: RegistryEntry<Biome>, random: Random): Pair<TraitStacks, Boolean> {

    // スポーン条件判定
    val aTraitStackList = mutableListOf<TraitStack>()
    val cTraitStackList = mutableListOf<TraitStack>()
    val nTraitStackList = mutableListOf<TraitStack>()
    val rTraitStackList = mutableListOf<TraitStack>()
    val sTraitStackList = mutableListOf<TraitStack>()
    possibleTraits.forEach { trait ->
        trait.spawnSpecs.forEach { spawnSpec ->
            if (spawnSpec.condition.canSpawn(biome)) {
                val traitStackList = when (spawnSpec.rarity) {
                    TraitSpawnRarity.ALWAYS -> aTraitStackList
                    TraitSpawnRarity.COMMON -> cTraitStackList
                    TraitSpawnRarity.NORMAL -> nTraitStackList
                    TraitSpawnRarity.RARE -> rTraitStackList
                    TraitSpawnRarity.S_RARE -> sTraitStackList
                }
                traitStackList += TraitStack(trait, spawnSpec.level)
            }
        }
    }

    // 抽選
    val resultTraitStackList = mutableListOf<TraitStack>()
    var isRare = false
    val r = random.nextDouble()
    when {
        r < 0.01 -> { // +S
            resultTraitStackList += aTraitStackList
            resultTraitStackList += cTraitStackList
            if (sTraitStackList.isNotEmpty()) {
                resultTraitStackList += sTraitStackList[random.nextInt(sTraitStackList.size)]
                isRare = true
            }
        }

        r >= 0.02 && r < 0.1 -> { // +R
            resultTraitStackList += aTraitStackList
            resultTraitStackList += cTraitStackList
            if (rTraitStackList.isNotEmpty()) {
                resultTraitStackList += rTraitStackList[random.nextInt(rTraitStackList.size)]
            }
        }

        r >= 0.01 && r < 0.02 -> { // -C
            resultTraitStackList += aTraitStackList
            if (cTraitStackList.isNotEmpty()) {
                cTraitStackList.removeAt(random.nextInt(cTraitStackList.size))
                resultTraitStackList += cTraitStackList
                isRare = true
            }
        }

        else -> { // +N
            resultTraitStackList += aTraitStackList
            resultTraitStackList += cTraitStackList
            if (nTraitStackList.isNotEmpty()) {
                resultTraitStackList += nTraitStackList[random.nextInt(nTraitStackList.size)]
            }
        }
    }

    return Pair(TraitStacks.of(resultTraitStackList), isRare)
}
