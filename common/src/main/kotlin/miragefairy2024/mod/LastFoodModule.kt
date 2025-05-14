package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.EatFoodCallback
import mirrg.kotlin.java.hydrogen.orNull
import mirrg.kotlin.java.hydrogen.toOptional
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import java.time.Instant
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.world.entity.player.Player as PlayerEntity

context(ModContext)
fun initLastFoodModule() {
    EatFoodCallback.EVENT.register { entity, world, stack, foodProperties ->
        if (world.isClientSide) return@register
        if (entity !is PlayerEntity) return@register
        entity as ServerPlayerEntity
        entity.lastFood = LastFood(stack.copy(), Instant.now())
    }
}

val LAST_FOOD_ATTACHMENT_TYPE: AttachmentType<LastFood> = AttachmentRegistry.create(MirageFairy2024.identifier("last_food")) {
    it.persistent(LastFood.CODEC)
    it.initializer { LastFood() }
    it.syncWith(LastFood.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
}

var Entity.lastFood
    get() = this.getAttached(LAST_FOOD_ATTACHMENT_TYPE)
    set(value) {
        this.setAttached(LAST_FOOD_ATTACHMENT_TYPE, value)
    }

class LastFood(val itemStack: ItemStack? = null, val time: Instant? = null) {
    companion object {
        val CODEC: Codec<LastFood> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.optionalFieldOf("item_stack").forGetter { it.itemStack.toOptional() },
                Codec.LONG.optionalFieldOf("time").xmap(
                    { it.map(Instant::ofEpochMilli) },
                    { it.map(Instant::toEpochMilli) },
                ).forGetter { it.time.toOptional() },
            ).apply(instance) { itemStack, time -> LastFood(itemStack.orNull, time.orNull) }
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, LastFood> = StreamCodec.composite(
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC),
            { it.itemStack.toOptional() },
            ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG).map(
                { it.map(Instant::ofEpochMilli) },
                { it.map(Instant::toEpochMilli) },
            ),
            { it.time.toOptional() },
        ) { itemStack, time -> LastFood(itemStack.orNull, time.orNull) }
    }
}
