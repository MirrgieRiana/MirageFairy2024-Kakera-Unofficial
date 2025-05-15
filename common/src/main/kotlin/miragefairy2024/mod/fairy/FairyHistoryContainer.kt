package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.get
import miragefairy2024.util.list
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity

val FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE: AttachmentType<FairyHistoryContainer> = AttachmentRegistry.create(MirageFairy2024.identifier("fairy_history_container")) {
    it.persistent(FairyHistoryContainer.CODEC)
    it.initializer(::FairyHistoryContainer)
    it.syncWith(FairyHistoryContainer.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
}

val Entity.fairyHistoryContainer get() = this[FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE]

class FairyHistoryContainer {
    companion object {
        val ENTRY_CODEC: Codec<Pair<ResourceLocation, Long>> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("motif").forGetter { it.first },
                Codec.LONG.fieldOf("gained").forGetter { it.second },
            ).apply(instance, ::Pair)
        }
        val ENTRY_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Pair<ResourceLocation, Long>> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            { it.first },
            ByteBufCodecs.VAR_LONG,
            { it.second },
            ::Pair,
        )

        fun fromEntries(entries: List<Pair<ResourceLocation, Long>>): FairyHistoryContainer {
            val fairyHistoryContainer = FairyHistoryContainer()
            entries.forEach { (key, count) ->
                val motif = motifRegistry[key] ?: return@forEach
                fairyHistoryContainer[motif] = count.toInt()
            }
            return fairyHistoryContainer
        }

        fun toEntries(fairyHistoryContainer: FairyHistoryContainer): List<Pair<ResourceLocation, Long>> {
            val entries = mutableListOf<Pair<ResourceLocation, Long>>()
            fairyHistoryContainer.entries.forEach { (motif, count) ->
                if (count > 0) entries += Pair(motif.getIdentifier()!!, count.toLong())
            }
            return entries
        }

        val CODEC: Codec<FairyHistoryContainer> = ENTRY_CODEC.listOf().xmap(::fromEntries, ::toEntries)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, FairyHistoryContainer> = ENTRY_STREAM_CODEC.list().map(::fromEntries, ::toEntries)
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
