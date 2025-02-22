package miragefairy2024.client.mod

import miragefairy2024.api.client.inputEventsHandlers
import miragefairy2024.client.util.sendToServer
import miragefairy2024.mod.placeditem.PLACE_ITEM_KEY_TRANSLATION
import miragefairy2024.mod.placeditem.PlaceItemChannel
import miragefairy2024.mod.placeditem.PlacedItemCard
import miragefairy2024.mod.placeditem.RemovePlacedItemChannel
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW

lateinit var placeItemKey: KeyBinding

fun initPlacedItemClientModule() {

    placeItemKey = KeyBinding(PLACE_ITEM_KEY_TRANSLATION.keyGetter(), GLFW.GLFW_KEY_Z, KeyBinding.GAMEPLAY_CATEGORY)
    inputEventsHandlers += {
        while (placeItemKey.wasPressed()) run {

            val player = MinecraftClient.getInstance().player ?: return@run // プレイヤーの取得に失敗した

            if (player.isSpectator) return@run // スペクテイターモード

            val hitResult = player.raycast(5.0, 0F, false)
            if (hitResult.type != HitResult.Type.BLOCK) return@run // ブロックをターゲットにしていない
            if (hitResult !is BlockHitResult) return@run // ブロックをターゲットにしていない

            if (!player.world.getBlockState(hitResult.blockPos).isOf(PlacedItemCard.block)) {
                val blockPos = if (player.world.getBlockState(hitResult.blockPos).isReplaceable) hitResult.blockPos else hitResult.blockPos.offset(hitResult.side)
                val rotation = when (hitResult.side) {
                    Direction.DOWN -> Pair(MathHelper.HALF_PI.toDouble(), -(player.yaw.toDouble() + 180.0) / 180.0 * MathHelper.PI)
                    Direction.UP, null -> Pair(-MathHelper.HALF_PI.toDouble(), -(player.yaw.toDouble() + 180.0) / 180.0 * MathHelper.PI)
                    Direction.NORTH -> Pair(0.0, 180.0 / 180.0 * MathHelper.PI)
                    Direction.SOUTH -> Pair(0.0, 0.0 / 180.0 * MathHelper.PI)
                    Direction.WEST -> Pair(0.0, 270.0 / 180.0 * MathHelper.PI)
                    Direction.EAST -> Pair(0.0, 90.0 / 180.0 * MathHelper.PI)
                }

                val packet = PlaceItemChannel.Packet(
                    blockPos,
                    hitResult.pos.x - blockPos.x.toDouble() atLeast 0.5 / 16.0 atMost 15.5 / 16.0,
                    hitResult.pos.y - blockPos.y.toDouble() atLeast 0.5 / 16.0 atMost 15.5 / 16.0,
                    hitResult.pos.z - blockPos.z.toDouble() atLeast 0.5 / 16.0 atMost 15.5 / 16.0,
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
