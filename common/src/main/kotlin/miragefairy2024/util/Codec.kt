package miragefairy2024.util

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents
import java.time.Instant
import java.util.Optional

fun <B : ByteBuf, V> StreamCodec<B, V>.optional(): StreamCodec<B, Optional<V>> = ByteBufCodecs.optional(this)
fun <B : ByteBuf, V> StreamCodec<B, V>.list(): StreamCodec<B, List<V>> = this.apply(ByteBufCodecs.list())

val INSTANT_CODEC: Codec<Instant> = Codec.LONG.xmap(Instant::ofEpochMilli, Instant::toEpochMilli)
val INSTANT_STREAM_CODEC: StreamCodec<ByteBuf, Instant> = ByteBufCodecs.VAR_LONG.map(Instant::ofEpochMilli, Instant::toEpochMilli)

val ITEMS_CODEC: Codec<List<ItemStack>> = ItemContainerContents.CODEC.xmap({ it.stream().toList() }, { ItemContainerContents.fromItems(it) })
val ITEMS_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> = ItemContainerContents.STREAM_CODEC.map({ it.stream().toList() }, { ItemContainerContents.fromItems(it) })
