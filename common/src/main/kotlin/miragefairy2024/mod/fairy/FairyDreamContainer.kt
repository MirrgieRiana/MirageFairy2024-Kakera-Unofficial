package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.list
import miragefairy2024.util.obtain
import miragefairy2024.util.sendToClient
import miragefairy2024.util.sync
import miragefairy2024.util.text
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

val FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE: AttachmentType<FairyDreamContainer> = AttachmentRegistry.create(MirageFairy2024.identifier("fairy_dream")) {
    it.persistent(FairyDreamContainer.CODEC)
    it.initializer(::FairyDreamContainer)
    it.syncWith(FairyDreamContainer.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.fairyDreamContainer get() = this[FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE]

class FairyDreamContainer {
    companion object {
        val ENTRY_CODEC: Codec<Pair<ResourceLocation, Boolean>> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("motif").forGetter { it.first },
                Codec.LONG.xmap({ it > 0 }, { if (it) 1 else 0 }).fieldOf("gained").forGetter { it.second },
            ).apply(instance, ::Pair)
        }
        val ENTRY_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Pair<ResourceLocation, Boolean>> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            { it.first },
            ByteBufCodecs.VAR_LONG.map({ it > 0 }, { if (it) 1 else 0 }),
            { it.second },
            ::Pair,
        )

        fun fromEntries(entries: List<Pair<ResourceLocation, Boolean>>): FairyDreamContainer {
            val fairyDreamContainer = FairyDreamContainer()
            entries.forEach { (key, value) ->
                val motif = motifRegistry[key] ?: return@forEach
                fairyDreamContainer[motif] = value
            }
            return fairyDreamContainer
        }

        fun toEntries(fairyDreamContainer: FairyDreamContainer): List<Pair<ResourceLocation, Boolean>> {
            val entries = mutableListOf<Pair<ResourceLocation, Boolean>>()
            fairyDreamContainer.entries.forEach { motif ->
                entries += Pair(motif.getIdentifier()!!, true)
            }
            return entries
        }

        val CODEC: Codec<FairyDreamContainer> = ENTRY_CODEC.listOf().xmap(::fromEntries, ::toEntries)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, FairyDreamContainer> = ENTRY_STREAM_CODEC.list().map(::fromEntries, ::toEntries)
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

    fun gain(player: ServerPlayerEntity, motifs: Set<Motif>): Int {
        val actualAdditionalMotifs = motifs - map
        actualAdditionalMotifs.forEach { motif ->
            set(motif, true)
            if (motif.rare <= 9) {
                player.obtain(motif.createFairyItemStack())
                player.displayClientMessage(text { GAIN_FAIRY_TRANSLATION(motif.displayName) }, true)
            }
            GainFairyDreamChannel.sendToClient(player, motif)
        }
        if (actualAdditionalMotifs.isNotEmpty()) player.fairyDreamContainer.sync()
        return actualAdditionalMotifs.size
    }

    fun clear() = map.clear()

}
