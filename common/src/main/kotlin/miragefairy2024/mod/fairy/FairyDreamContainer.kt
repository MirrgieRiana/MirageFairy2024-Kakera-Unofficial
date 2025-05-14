package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.sync
import miragefairy2024.util.boolean
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.obtain
import miragefairy2024.util.sendToClient
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.core.HolderLookup
import net.minecraft.world.entity.Entity
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

val FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE: AttachmentType<FairyDreamContainer> = AttachmentRegistry.create(MirageFairy2024.identifier("fairy_dream")) {
    it.persistent(FairyDreamContainer.CODEC)
    it.initializer { FairyDreamContainer() }
    it.syncWith(FairyDreamContainer.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
}

var Entity.fairyDreamContainer
    get() = this.getAttached(FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE)
    set(value) {
        this.setAttached(FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE, value)
    }

class FairyDreamContainer {
    companion object {
        override fun fromNbt(nbt: NbtCompound, registry: HolderLookup.Provider): FairyDreamContainer {
            val data = FairyDreamContainer()
            nbt.allKeys.forEach { key ->
                val motif = motifRegistry[key.toIdentifier()] ?: return@forEach
                data[motif] = nbt.wrapper[key].boolean.get() ?: false
            }
            return data
        }

        override fun toNbt(data: FairyDreamContainer, registry: HolderLookup.Provider): NbtCompound {
            val nbt = NbtCompound()
            data.entries.forEach { motif ->
                nbt.wrapper[motif.getIdentifier()!!.string].boolean.set(true)
            }
            return nbt
        }
    }

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

    fun gain(player: ServerPlayerEntity, motifs: Iterable<Motif>) {
        val actualAdditionalMotifs = motifs - map
        actualAdditionalMotifs.forEach { motif ->
            set(motif, true)
            if (motif.rare <= 9) {
                player.obtain(motif.createFairyItemStack())
                player.displayClientMessage(text { GAIN_FAIRY_TRANSLATION(motif.displayName) }, true)
            }
            GainFairyDreamChannel.sendToClient(player, motif)
        }
        if (actualAdditionalMotifs.isNotEmpty()) FairyDreamContainerExtraPlayerDataCategory.sync(player)
    }

    fun clear() = map.clear()

}
