package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.mixin.api.EatFoodCallback
import miragefairy2024.util.compound
import miragefairy2024.util.get
import miragefairy2024.util.register
import miragefairy2024.util.toItemStack
import miragefairy2024.util.toNbt
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.GameRules

fun initLastFoodModule() {

    // 拡張プレイヤーデータ
    LastFoodExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, Identifier(MirageFairy2024.modId, "last_food"))

    // 食べ物を食べるとlastFoodをそれにする
    EatFoodCallback.EVENT.register { entity, world, stack ->
        if (world.isClient) return@register
        if (entity !is PlayerEntity) return@register
        entity as ServerPlayerEntity
        if (!stack.isFood) return@register
        entity.lastFood.itemStack = stack.copy()
        LastFoodExtraPlayerDataCategory.sync(entity)
    }

    // プレイヤーが死ぬとリセット
    ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
        if (entity !is PlayerEntity) return@register
        entity as ServerPlayerEntity
        if (entity.isSpectator) return@register
        if (entity.world.gameRules.getBoolean(GameRules.KEEP_INVENTORY)) return@register
        entity.lastFood.itemStack = null
        LastFoodExtraPlayerDataCategory.sync(entity)
    }

}


// 拡張プレイヤーデータ

object LastFoodExtraPlayerDataCategory : ExtraPlayerDataCategory<LastFood> {
    override fun create() = LastFood()
    override fun castOrThrow(value: Any) = value as LastFood
    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<LastFood> {
        override fun fromNbt(nbt: NbtCompound): LastFood {
            val data = LastFood()
            data.itemStack = nbt.wrapper["ItemStack"].compound.get()?.toItemStack()
            return data
        }

        override fun toNbt(data: LastFood): NbtCompound {
            val nbt = NbtCompound()
            nbt.wrapper["ItemStack"].compound.set(data.itemStack?.toNbt())
            return nbt
        }
    }
}

class LastFood(var itemStack: ItemStack? = null)

val PlayerEntity.lastFood get() = this.extraPlayerDataContainer.getOrInit(LastFoodExtraPlayerDataCategory)
