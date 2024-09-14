package miragefairy2024.mod.magicplant

import miragefairy2024.util.bitCount
import miragefairy2024.util.toNbtList
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import java.util.SortedMap

// api

class TraitStacks private constructor(val traitStackMap: SortedMap<Trait, Int>) {
    companion object {
        fun of(traitStackList: Iterable<TraitStack>): TraitStacks {
            // 同じ特性をまとめて、各レベルをビットORする
            val traitStackMap = traitStackList
                .groupBy { it.trait }
                .mapValues {
                    it.value
                        .map { traitStack -> traitStack.level }
                        .reduce { a, b -> a or b }
                }
                .toSortedMap()
            return TraitStacks(traitStackMap)
        }

        fun of(vararg traitStacks: TraitStack) = of(traitStacks.asIterable())

        fun of(traitStacks: Map<Trait, Int>) = TraitStacks(traitStacks.toSortedMap())

        val EMPTY = of()

        fun readFromNbt(parent: NbtCompound, key: String = "TraitStacks"): TraitStacks? {
            if (!parent.contains(key, NbtElement.LIST_TYPE.toInt())) return null
            return parent.getList(key, NbtElement.COMPOUND_TYPE.toInt()).toTraitStacks()
        }
    }

    init {
        traitStackMap.forEach { (_, level) ->
            require(level >= 1)
        }
    }

    val traitStackList by lazy { traitStackMap.map { TraitStack(it.key, it.value) } }
}


// util

fun NbtList.toTraitStacks(): TraitStacks {
    val traitStackList = (0..<this.size).mapNotNull {
        this.getCompound(it).toTraitStack()
    }
    return TraitStacks.of(traitStackList)
}

fun TraitStacks.toNbt() = this.traitStackMap.map { TraitStack(it.key, it.value).toNbt() }.toNbtList()

val TraitStacks.bitCount get() = this.traitStackList.sumOf { it.level.bitCount }

operator fun TraitStacks.plus(other: TraitStacks): TraitStacks {
    val map = this.traitStackMap.toMutableMap()
    other.traitStackMap.forEach { (trait, level) ->
        map[trait] = map.getOrDefault(trait, 0) or level
    }
    return TraitStacks.of(map)
}

operator fun TraitStacks.minus(other: TraitStacks): TraitStacks {
    val map = this.traitStackMap.toMutableMap()
    other.traitStackMap.forEach { (trait, level) ->
        val level2 = map.getOrDefault(trait, 0) and level.inv()
        if (level2 == 0) {
            map.remove(trait)
        } else {
            map[trait] = level2
        }
    }
    return TraitStacks.of(map)
}
