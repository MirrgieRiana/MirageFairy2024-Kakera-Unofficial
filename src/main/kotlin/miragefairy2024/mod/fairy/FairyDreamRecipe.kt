package miragefairy2024.mod.fairy

import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.TagKey
import kotlin.jvm.optionals.getOrElse

object FairyDreamRecipes {
    val ITEM = FairyDreamTable<Item>(Registries.ITEM)
    val BLOCK = FairyDreamTable<Block>(Registries.BLOCK)
    val ENTITY_TYPE = FairyDreamTable<EntityType<*>>(Registries.ENTITY_TYPE)
}

class FairyDreamTable<T>(val registry: Registry<T>) {
    private val map = mutableMapOf<T, MutableSet<Motif>>()
    private val displayMap = mutableListOf<Pair<T, Motif>>()
    private val displayTagMap = mutableListOf<Pair<TagKey<T>, Motif>>()

    fun register(key: T, motif: Motif) {
        map.getOrPut(key) { mutableSetOf() } += motif
        displayMap += Pair(key, motif)
    }

    fun registerFromTag(tag: TagKey<T>, motif: Motif) {
        registry.getEntryList(tag).getOrElse { return }.map { it.value() }.forEach {
            map.getOrPut(it) { mutableSetOf() } += motif
        }
        displayTagMap += Pair(tag, motif)
    }

    fun test(key: T): Set<Motif> = map.getOrElse(key) { setOf() }

    fun getDisplayMap(): List<Pair<T, Motif>> = displayMap
    fun getDisplayTagMap(): List<Pair<TagKey<T>, Motif>> = displayTagMap
}
