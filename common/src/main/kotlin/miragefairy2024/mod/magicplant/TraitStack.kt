package miragefairy2024.mod.magicplant

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.nbt.CompoundTag as NbtCompound

// api

data class TraitStack(val trait: Trait, val level: Int) {
    companion object {
        val CODEC: Codec<TraitStack> = RecordCodecBuilder.create { instance ->
            instance.group(
                Trait.CODEC.fieldOf("trait").forGetter { it.trait },
                Codec.intRange(0, Int.MAX_VALUE).fieldOf("level").forGetter { it.level },
            ).apply(instance, ::TraitStack)
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TraitStack> = StreamCodec.composite(Trait.STREAM_CODEC, { it.trait }, ByteBufCodecs.INT, { it.level }, ::TraitStack)
    }

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
