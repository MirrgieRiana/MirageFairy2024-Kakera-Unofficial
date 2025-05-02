package miragefairy2024.mod.magicplant

import com.mojang.serialization.Codec
import miragefairy2024.util.bitCount
import miragefairy2024.util.toNbtList
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.SortedMap
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.nbt.ListTag as NbtList
import net.minecraft.nbt.Tag as NbtElement

// api

class TraitStacks private constructor(val traitStackMap: SortedMap<Trait, Int>) {
    companion object {
        val CODEC: Codec<TraitStacks> = TraitStack.CODEC.listOf().xmap({ of(it) }, { it.traitStackList })
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TraitStacks> = TraitStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map({ of(it) }, { it.traitStackList })

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
            if (!parent.contains(key, NbtElement.TAG_LIST.toInt())) return null
            return parent.getList(key, NbtElement.TAG_COMPOUND.toInt()).toTraitStacks()
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
