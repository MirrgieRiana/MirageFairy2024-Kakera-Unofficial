package miragefairy2024.util

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString

fun Collection<NbtElement>.toNbtList(): NbtList {
    val nbtList = NbtList()
    nbtList.addAll(this)
    return nbtList
}

fun Iterable<Pair<String, NbtElement>>.toNbtCompound(): NbtCompound {
    val nbtCompound = NbtCompound()
    this.forEach {
        nbtCompound.put(it.first, it.second)
    }
    return nbtCompound
}

fun NbtList(vararg elements: NbtElement) = elements.toList().toNbtList()
fun NbtCompound(vararg entries: Pair<String, NbtElement>) = entries.toList().toNbtCompound()

fun String.toNbtString(): NbtString = NbtString.of(this)
