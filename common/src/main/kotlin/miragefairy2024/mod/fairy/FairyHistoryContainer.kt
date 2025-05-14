package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.core.HolderLookup
import net.minecraft.world.entity.Entity
import net.minecraft.nbt.CompoundTag as NbtCompound

val FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE: AttachmentType<FairyHistoryContainer> = AttachmentRegistry.create(MirageFairy2024.identifier("fairy_history_container")) {
    it.persistent(FairyHistoryContainer.CODEC)
    it.initializer { FairyHistoryContainer() }
    it.syncWith(FairyHistoryContainer.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
}

var Entity.fairyHistoryContainer
    get() = this.getAttached(FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE)
    set(value) {
        this.setAttached(FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE, value)
    }

class FairyHistoryContainer {
    companion object {
        override fun fromNbt(nbt: NbtCompound, registry: HolderLookup.Provider): FairyHistoryContainer {
            val data = FairyHistoryContainer()
            nbt.allKeys.forEach { key ->
                val motif = motifRegistry[key.toIdentifier()] ?: return@forEach
                data[motif] = nbt.wrapper[key].int.get()
            }
            return data
        }

        override fun toNbt(data: FairyHistoryContainer, registry: HolderLookup.Provider): NbtCompound {
            val nbt = NbtCompound()
            data.entries.forEach { (motif, count) ->
                if (count > 0) nbt.wrapper[motif.getIdentifier()!!.string].int.set(count)
            }
            return nbt
        }
    }

    private val map = mutableMapOf<Motif, Int>()

    val entries get() = map.entries

    operator fun get(motif: Motif) = map.getOrElse(motif) { 0 }

    operator fun set(motif: Motif, count: Int?) {
        if (count != null) {
            check(count >= 0)
            if (count == 0) {
                map.remove(motif)
            } else {
                map[motif] = count
            }
        } else {
            map.remove(motif)
        }
    }
}
