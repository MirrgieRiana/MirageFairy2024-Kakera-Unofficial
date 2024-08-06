package miragefairy2024.mod.magicplant

import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.minecraft.nbt.NbtCompound

// api

class TraitStack(val trait: Trait, val level: Int) {
    init {
        require(level >= 1)
    }
}


// util

fun NbtCompound.toTraitStack(): TraitStack? {
    val trait = this.wrapper["Trait"].string.get()?.toIdentifier()?.toTrait() ?: return null
    val level = this.wrapper["Level"].int.get() ?: return null
    if (level < 1) return null
    return TraitStack(trait, level)
}

fun TraitStack.toNbt() = NbtCompound().also {
    it.wrapper["Trait"].string.set(this.trait.getIdentifier().string)
    it.wrapper["Level"].int.set(this.level)
}
