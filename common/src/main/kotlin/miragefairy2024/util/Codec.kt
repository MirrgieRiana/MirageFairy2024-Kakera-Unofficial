package miragefairy2024.util

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
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

/**
 * ダミーのペイロードを追加するStreamCodecです。
 * NeoForgeにおいてMenuを開く際にペイロードが空であるとバニラの機構を使うため、これを抑制するために用いられます。
 *
 * @see net.minecraft.server.level.ServerPlayer.openMenu
 */
fun dummyUnitStreamCodec(): StreamCodec<ByteBuf, Unit> = ByteBufCodecs.VAR_INT.map({ }, { 0 })

fun <T> ResourceKey<Registry<T>>.toHolderSetCodec(): Codec<HolderSet<T>> = RegistryCodecs.homogeneousList(this)
