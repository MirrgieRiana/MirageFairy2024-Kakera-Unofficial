package miragefairy2024.mod.fairy

import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

object FairyDreamContainerExtraPlayerDataCategory : ExtraPlayerDataCategory<FairyDreamContainer> {
    override fun castOrThrow(value: Any?) = value as FairyDreamContainer
    override fun fromNbt(player: PlayerEntity, nbt: NbtCompound): FairyDreamContainer {
        val data = FairyDreamContainer()
        nbt.keys.forEach { key ->
            val motif = motifRegistry[key.toIdentifier()] ?: return@forEach
            data[motif] = nbt.getBoolean(key)
        }
        return data
    }

    override fun toNbt(player: PlayerEntity, data: FairyDreamContainer): NbtCompound {
        val nbt = NbtCompound()
        data.entries.forEach { motif ->
            nbt.putBoolean(motif.getIdentifier()!!.string, true)
        }
        return nbt
    }
}

val PlayerEntity.fairyDreamContainer get() = this.extraPlayerDataContainer[FairyDreamContainerExtraPlayerDataCategory]

class FairyDreamContainer {
    private val map = mutableSetOf<Motif>()

    operator fun get(motif: Motif) = motif in map

    operator fun set(motif: Motif, value: Boolean) {
        if (value) {
            map += motif
        } else {
            map.remove(motif)
        }
    }

    val entries: Set<Motif> get() = map
}
