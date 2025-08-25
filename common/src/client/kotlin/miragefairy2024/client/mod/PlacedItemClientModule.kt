package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.util.sendToServer
import miragefairy2024.mixin.client.api.inputEventsHandlers
import miragefairy2024.mod.placeditem.PLACE_ITEM_KEY_TRANSLATION
import miragefairy2024.mod.placeditem.PlaceItemChannel
import miragefairy2024.mod.placeditem.PlacedItemCard
import miragefairy2024.mod.placeditem.RemovePlacedItemChannel
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.core.Direction
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import org.lwjgl.glfw.GLFW
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.util.Mth as MathHelper

lateinit var placeItemKey: KeyMapping

context(ModContext)
fun initPlacedItemClientModule() {

    placeItemKey = KeyMapping(PLACE_ITEM_KEY_TRANSLATION.keyGetter(), GLFW.GLFW_KEY_Z, KeyMapping.CATEGORY_GAMEPLAY)
    inputEventsHandlers += {
        while (placeItemKey.consumeClick()) run {

            val player = MinecraftClient.getInstance().player ?: return@run // プレイヤーの取得に失敗した

            if (player.isSpectator) return@run // スペクテイターモード

            val hitResult = player.pick(player.blockInteractionRange(), 0F, false)
            if (hitResult.type != HitResult.Type.BLOCK) return@run // ブロックをターゲットにしていない
            if (hitResult !is BlockHitResult) return@run // ブロックをターゲットにしていない

            if (!player.level().getBlockState(hitResult.blockPos).`is`(PlacedItemCard.block())) {
                val blockPos = if (player.level().getBlockState(hitResult.blockPos).canBeReplaced()) hitResult.blockPos else hitResult.blockPos.relative(hitResult.direction)
                val rotation = when (hitResult.direction) {
                    Direction.DOWN -> Pair(MathHelper.HALF_PI.toDouble(), -(player.yRot.toDouble() + 180.0) / 180.0 * MathHelper.PI)
                    Direction.UP, null -> Pair(-MathHelper.HALF_PI.toDouble(), -(player.yRot.toDouble() + 180.0) / 180.0 * MathHelper.PI)
                    Direction.NORTH -> Pair(0.0, 180.0 / 180.0 * MathHelper.PI)
                    Direction.SOUTH -> Pair(0.0, 0.0 / 180.0 * MathHelper.PI)
                    Direction.WEST -> Pair(0.0, 270.0 / 180.0 * MathHelper.PI)
                    Direction.EAST -> Pair(0.0, 90.0 / 180.0 * MathHelper.PI)
                }

                val packet = PlaceItemChannel.Packet(
                    blockPos,
                    hitResult.location.x - blockPos.x.toDouble() atLeast 0.5 / 16.0 atMost 15.5 / 16.0,
                    hitResult.location.y - blockPos.y.toDouble() atLeast 0.5 / 16.0 atMost 15.5 / 16.0,
                    hitResult.location.z - blockPos.z.toDouble() atLeast 0.5 / 16.0 atMost 15.5 / 16.0,
                    rotation.first,
                    rotation.second,
                )

                PlaceItemChannel.sendToServer(packet)
            } else {
                RemovePlacedItemChannel.sendToServer(RemovePlacedItemChannel.Packet(hitResult.blockPos))
            }
        }
    }
    KeyBindingHelper.registerKeyBinding(placeItemKey)

}
