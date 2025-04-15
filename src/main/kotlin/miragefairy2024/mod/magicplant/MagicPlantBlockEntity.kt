package miragefairy2024.mod.magicplant

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.network.protocol.game.ClientGamePacketListener as ClientPlayPacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket as BlockEntityUpdateS2CPacket
import net.minecraft.core.Holder as RegistryEntry
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.BlockGetter as BlockView
import net.minecraft.world.level.Level as World
import net.minecraft.world.level.biome.Biome

class MagicPlantBlockEntity(private val configuration: MagicPlantConfiguration<*, *>, pos: BlockPos, state: BlockState) : BlockEntity(configuration.card.blockEntityType, pos, state) {

    private var traitStacks: TraitStacks? = null

    fun getTraitStacks() = traitStacks

    fun setTraitStacks(traitStacks: TraitStacks) {
        this.traitStacks = traitStacks
        setChanged()
    }


    private var isRare = false

    fun isRare() = isRare

    fun setRare(isRare: Boolean) {
        this.isRare = isRare
        setChanged()
    }


    private var isNatural = false

    fun isNatural() = isNatural

    fun setNatural(isNatural: Boolean) {
        this.isNatural = isNatural
        setChanged()
    }


    override fun setWorld(world: World) {
        super.setWorld(world)
        if (traitStacks == null) {
            val result = spawnTraitStacks(configuration.possibleTraits, world.getBiome(worldPosition), world.random)
            setTraitStacks(result.first)
            setRare(result.second)
            setNatural(true)
        }
    }

    public override fun saveAdditional(nbt: NbtCompound) {
        super.saveAdditional(nbt)
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
        if (isNatural) nbt.putBoolean("Natural", true)
    }

    override fun load(nbt: NbtCompound) {
        super.load(nbt)
        traitStacks = TraitStacks.readFromNbt(nbt)
        isRare = nbt.getBoolean("Rare")
        isNatural = nbt.getBoolean("Natural")
    }

    override fun getUpdateTag(): NbtCompound {
        val nbt = super.getUpdateTag()
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
        if (isNatural) nbt.putBoolean("Natural", true)
        return nbt
    }

    override fun getUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)

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
