package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.EatFoodCallback
import miragefairy2024.util.Registration
import miragefairy2024.util.get
import miragefairy2024.util.long
import miragefairy2024.util.register
import miragefairy2024.util.toItemStack
import miragefairy2024.util.toNbt
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import java.time.Instant
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.world.entity.player.Player as PlayerEntity

context(ModContext)
fun initLastFoodModule() {

    // 拡張プレイヤーデータ
    Registration(extraPlayerDataCategoryRegistry, MirageFairy2024.identifier("last_food")) { LastFoodExtraPlayerDataCategory }.register()

    EatFoodCallback.EVENT.register { entity, world, stack, foodProperties ->
        if (world.isClientSide) return@register
        if (entity !is PlayerEntity) return@register
        entity as ServerPlayerEntity
        entity.lastFood.itemStack = stack.copy()
        entity.lastFood.time = Instant.now()
        LastFoodExtraPlayerDataCategory.sync(entity)
    }

    // プレイヤーが死ぬとリセット
    ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
        if (entity !is PlayerEntity) return@register
        entity as ServerPlayerEntity
        if (entity.isSpectator) return@register
        if (entity.level().gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)) return@register
        entity.lastFood.itemStack = null
        entity.lastFood.time = null
        LastFoodExtraPlayerDataCategory.sync(entity)
    }

}


// 拡張プレイヤーデータ

object LastFoodExtraPlayerDataCategory : ExtraPlayerDataCategory<LastFood> {
    override fun create() = LastFood()
    override fun castOrThrow(value: Any) = value as LastFood
    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<LastFood> {
        override fun fromNbt(nbt: NbtCompound, registry: HolderLookup.Provider): LastFood {
            val data = LastFood()
            data.itemStack = nbt.wrapper["ItemStack"].get()?.toItemStack(registry)
            data.time = nbt.wrapper["Time"].long.get()?.let { Instant.ofEpochMilli(it) }
            return data
        }

        override fun toNbt(data: LastFood, registry: HolderLookup.Provider): NbtCompound {
            val nbt = NbtCompound()
            nbt.wrapper["ItemStack"].set(data.itemStack?.toNbt(registry))
            nbt.wrapper["Time"].long.set(data.time?.toEpochMilli())
            return nbt
        }
    }
}

class LastFood(var itemStack: ItemStack? = null, var time: Instant? = null)

val PlayerEntity.lastFood get() = this.extraPlayerDataContainer.getOrInit(LastFoodExtraPlayerDataCategory)
