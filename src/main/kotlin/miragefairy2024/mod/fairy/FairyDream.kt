package miragefairy2024.mod.fairy

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.sync
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.itemStacks
import miragefairy2024.util.opposite
import miragefairy2024.util.registerServerDebugItem
import miragefairy2024.util.sendToClient
import miragefairy2024.util.text
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.world.RaycastContext

val GAIN_FAIRY_DREAM_TRANSLATION = Translation({ "gui.miragefairy2024.fairy_dream.gain" }, "Dreamed of a new fairy!", "新たな妖精の夢を見た！")
val GAIN_FAIRY_TRANSLATION = Translation({ "gui.miragefairy2024.fairy_dream.gain_fairy" }, "%s found!", "%sを発見した！")

context(ModContext)
fun initFairyDream() {

    // デバッグアイテム
    registerServerDebugItem("debug_clear_fairy_dream", Items.STRING, 0x0000DD) { world, player, _, _ ->
        player.fairyDreamContainer.clear()
        player.sendMessage(text { "Cleared fairy dream"() }, true)
    }
    registerServerDebugItem("debug_gain_fairy_dream", Items.STRING, 0x0000BB) { world, player, hand, _ ->
        val fairyItemStack = player.getStackInHand(hand.opposite)
        if (!fairyItemStack.isOf(FairyCard.item)) return@registerServerDebugItem
        val motif = fairyItemStack.getFairyMotif() ?: return@registerServerDebugItem

        if (!player.isSneaking) {
            player.fairyDreamContainer[motif] = true
            GainFairyDreamChannel.sendToClient(player, motif)
        } else {
            player.fairyDreamContainer[motif] = false
            FairyDreamContainerExtraPlayerDataCategory.sync(player)
        }
    }

    // 妖精の夢回収判定
    ModEvents.onInitialize {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (server.ticks % (20 * 5) == 0) {
                server.playerManager.playerList.forEach { player ->
                    if (player.isSpectator) return@forEach
                    val world = player.world
                    val random = world.random

                    val items = mutableSetOf<Item>()
                    val blocks = mutableSetOf<Block>()
                    val entityTypes = mutableSetOf<EntityType<*>>()
                    run {

                        fun insertItem(item: Item) {
                            items += item

                            val block = Block.getBlockFromItem(item)
                            if (block != Blocks.AIR) blocks += block
                        }

                        fun insertBlockPos(blockPos: BlockPos) {
                            blocks += world.getBlockState(blockPos).block
                        }


                        // インベントリ判定
                        player.inventory.itemStacks.forEach { itemStack ->
                            insertItem(itemStack.item)
                        }

                        // 足元判定
                        insertBlockPos(player.blockPos)
                        insertBlockPos(player.blockPos.down())

                        // 視線判定
                        val start = player.eyePos
                        val pitch = player.pitch
                        val yaw = player.yaw
                        val d = MathHelper.cos(-yaw * (MathHelper.PI / 180) - MathHelper.PI)
                        val a = MathHelper.sin(-yaw * (MathHelper.PI / 180) - MathHelper.PI)
                        val e = -MathHelper.cos(-pitch * (MathHelper.PI / 180))
                        val c = MathHelper.sin(-pitch * (MathHelper.PI / 180))
                        val end = start.add(a * e * 32.0, c * 32.0, d * e * 32.0)
                        val raycastResult = world.raycast(RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player))
                        if (raycastResult.type == HitResult.Type.BLOCK) insertBlockPos(raycastResult.blockPos)

                        // 周辺エンティティ判定
                        val entities = world.getOtherEntities(player, Box(player.eyePos.add(-8.0, -8.0, -8.0), player.eyePos.add(8.0, 8.0, 8.0)))
                        entities.forEach {
                            entityTypes += it.type
                        }

                        // 周辺ブロック判定
                        insertBlockPos(player.eyeBlockPos.add(random.nextInt(17) - 8, random.nextInt(17) - 8, random.nextInt(17) - 8))

                    }

                    val motifs = mutableSetOf<Motif>()
                    items.forEach {
                        motifs += FairyDreamRecipes.ITEM.test(it)
                    }
                    blocks.forEach {
                        motifs += FairyDreamRecipes.BLOCK.test(it)
                    }
                    entityTypes.forEach {
                        motifs += FairyDreamRecipes.ENTITY_TYPE.test(it)
                    }

                    player.fairyDreamContainer.gain(player, motifs)

                }
            }
        }
    }

    // 翻訳
    GAIN_FAIRY_DREAM_TRANSLATION.enJa()
    GAIN_FAIRY_TRANSLATION.enJa()

}
