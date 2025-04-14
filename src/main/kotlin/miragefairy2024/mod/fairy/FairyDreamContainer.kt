package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.mod.sync
import miragefairy2024.util.boolean
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.obtain
import miragefairy2024.util.register
import miragefairy2024.util.sendToClient
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

context(ModContext)
fun initFairyDreamContainer() {
    FairyDreamContainerExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, MirageFairy2024.identifier("fairy_dream"))
}

object FairyDreamContainerExtraPlayerDataCategory : ExtraPlayerDataCategory<FairyDreamContainer> {
    override fun create() = FairyDreamContainer()
    override fun castOrThrow(value: Any) = value as FairyDreamContainer
    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<FairyDreamContainer> {
        override fun fromNbt(nbt: NbtCompound): FairyDreamContainer {
            val data = FairyDreamContainer()
            nbt.allKeys.forEach { key ->
                val motif = motifRegistry[key.toIdentifier()] ?: return@forEach
                data[motif] = nbt.wrapper[key].boolean.get() ?: false
            }
            return data
        }

        override fun toNbt(data: FairyDreamContainer): NbtCompound {
            val nbt = NbtCompound()
            data.entries.forEach { motif ->
                nbt.wrapper[motif.getIdentifier()!!.string].boolean.set(true)
            }
            return nbt
        }
    }
}

val PlayerEntity.fairyDreamContainer get() = this.extraPlayerDataContainer.getOrInit(FairyDreamContainerExtraPlayerDataCategory)

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

    fun gain(player: ServerPlayerEntity, motifs: Iterable<Motif>) {
        val actualAdditionalMotifs = motifs - map
        actualAdditionalMotifs.forEach { motif ->
            set(motif, true)
            if (motif.rare <= 9) {
                player.obtain(motif.createFairyItemStack())
                player.sendMessage(text { GAIN_FAIRY_TRANSLATION(motif.displayName) }, true)
            }
            GainFairyDreamChannel.sendToClient(player, motif)
        }
        if (actualAdditionalMotifs.isNotEmpty()) FairyDreamContainerExtraPlayerDataCategory.sync(player)
    }

    fun clear() = map.clear()

}
