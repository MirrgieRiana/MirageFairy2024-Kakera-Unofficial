package miragefairy2024.mod.fairy

import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.util.boolean
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

object FairyDreamContainerExtraPlayerDataCategory : ExtraPlayerDataCategory<FairyDreamContainer> {
    override fun castOrThrow(value: Any?) = value as FairyDreamContainer
    override fun fromNbt(player: PlayerEntity, nbt: NbtCompound): FairyDreamContainer {
        val data = FairyDreamContainer()
        nbt.keys.forEach { key ->
            val motif = motifRegistry[key.toIdentifier()] ?: return@forEach
            data[motif] = nbt.wrapper[key].boolean.get() ?: false
        }
        return data
    }

    override fun toNbt(player: PlayerEntity, data: FairyDreamContainer): NbtCompound {
        val nbt = NbtCompound()
        data.entries.forEach { motif ->
            nbt.wrapper[motif.getIdentifier()!!.string].boolean.set(true)
        }
        return nbt
    }
}

val PlayerEntity.fairyDreamContainer get() = this.extraPlayerDataContainer[FairyDreamContainerExtraPlayerDataCategory]

class FairyDreamContainer {

    private val map = mutableSetOf<Motif>()

    operator fun get(motif: Motif) = motif in map

    val entries: Set<Motif> get() = map

    operator fun set(motif: Motif, value: Boolean) {
        if (value) {
            map += motif
        } else {
            map.remove(motif)
        }
    }

    fun clear() = map.clear()

}
