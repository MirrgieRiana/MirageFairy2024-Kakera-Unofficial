package miragefairy2024.mod.fairy

import mirrg.kotlin.hydrogen.unit
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
    private val entries = mutableListOf<Pair<T, Motif>>()
    private val tagEntries = mutableListOf<Pair<TagKey<T>, Motif>>()

    private val mapPair: Pair<Map<T, Set<Motif>>, Map<Motif, Set<T>>> by lazy {
        val map = mutableMapOf<T, MutableSet<Motif>>()
        val reverseMap = mutableMapOf<Motif, MutableSet<T>>()
        entries.forEach { (key, motif) ->
            map.getOrPut(key) { mutableSetOf() } += motif
            reverseMap.getOrPut(motif) { mutableSetOf() } += key
        }
        tagEntries.forEach { (tag, motif) ->
            registry.getEntryList(tag).getOrElse { return@forEach }.map { it.value() }.forEach { key ->
                map.getOrPut(key) { mutableSetOf() } += motif
                reverseMap.getOrPut(motif) { mutableSetOf() } += key
            }
        }
        Pair(map, reverseMap)
    }

    fun register(key: T, motif: Motif) = unit { entries += Pair(key, motif) }
    fun registerFromTag(tag: TagKey<T>, motif: Motif) = unit { tagEntries += Pair(tag, motif) }

    fun test(key: T) = mapPair.first.getOrElse(key) { setOf() }
    fun getDisplayMap() = mapPair.second
}
