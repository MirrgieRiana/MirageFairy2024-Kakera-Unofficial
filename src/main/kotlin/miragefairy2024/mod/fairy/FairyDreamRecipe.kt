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

    fun register(key: T, motif: Motif) {
        map.getOrPut(key) { mutableSetOf() } += motif
    }

    fun registerFromTag(tag: TagKey<T>, motif: Motif) {
        registry.getEntryList(tag).getOrElse { return }.map { it.value() }.forEach {
            register(it, motif)
        }
    }

    fun test(key: T): Set<Motif> = map.getOrElse(key) { setOf() }
}
