package miragefairy2024.util

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag

fun Collection<Tag>.toNbtList(): ListTag {
    val nbtList = ListTag()
    nbtList.addAll(this)
    return nbtList
}

fun Iterable<Pair<String, Tag>>.toCompoundTag(): CompoundTag {
    val CompoundTag = CompoundTag()
    this.forEach {
        CompoundTag.put(it.first, it.second)
    }
    return CompoundTag
}

fun NbtList(vararg elements: Tag) = elements.toList().toNbtList()
fun CompoundTag(vararg entries: Pair<String, Tag>) = entries.toList().toCompoundTag()

fun String.toNbtString(): StringTag = StringTag.valueOf(this)
