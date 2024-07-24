package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.register
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

context(ModContext)
fun initFairyHistoryContainer() {
    FairyHistoryContainerExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, Identifier(MirageFairy2024.modId, "fairy_history_container"))
}

object FairyHistoryContainerExtraPlayerDataCategory : ExtraPlayerDataCategory<FairyHistoryContainer> {
    override fun create() = FairyHistoryContainer()
    override fun castOrThrow(value: Any) = value as FairyHistoryContainer
    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<FairyHistoryContainer> {
        override fun fromNbt(nbt: NbtCompound): FairyHistoryContainer {
            val data = FairyHistoryContainer()
            nbt.keys.forEach { key ->
                val motif = motifRegistry[key.toIdentifier()] ?: return@forEach
                data.set(motif, nbt.wrapper[key].int.get())
            }
            return data
        }

        override fun toNbt(data: FairyHistoryContainer): NbtCompound {
            val nbt = NbtCompound()
            data.entries.forEach { (motif, count) ->
                if (count > 0) nbt.wrapper[motif.getIdentifier()!!.string].int.set(count)
            }
            return nbt
        }
    }
}

class FairyHistoryContainer {
    private val map = mutableMapOf<Motif, Int>()

    val entries get() = map.entries

    fun add(motif: Motif, count: Int) {
        map[motif] = map.getOrElse(motif) { 0 } + count
    }

    fun set(motif: Motif, count: Int?) {
        if (count != null) {
            map[motif] = count
        } else {
            map.remove(motif)
        }
    }
}

val PlayerEntity.fairyHistoryContainer get() = this.extraPlayerDataContainer.getOrInit(FairyHistoryContainerExtraPlayerDataCategory)
