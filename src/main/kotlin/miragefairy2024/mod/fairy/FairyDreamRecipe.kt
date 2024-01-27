package miragefairy2024.mod.fairy

import miragefairy2024.util.itemStacks
import miragefairy2024.util.toBlockPos
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.world.RaycastContext
import kotlin.jvm.optionals.getOrNull

object FairyDreamRecipes {
    val ITEM = FairyDreamTable<Item>(Registries.ITEM)
    val BLOCK = FairyDreamTable<Block>(Registries.BLOCK)
    val ENTITY_TYPE = FairyDreamTable<EntityType<*>>(Registries.ENTITY_TYPE)
}

fun initFairyDreamRecipe() {
    ServerTickEvents.END_SERVER_TICK.register { server ->
        if (server.ticks % (20 * 5) == 0) {
            server.playerManager.playerList.forEach { player ->
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
                    insertBlockPos(player.eyePos.toBlockPos().add(random.nextInt(17) - 8, random.nextInt(17) - 8, random.nextInt(17) - 8))

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

class FairyDreamTable<T>(val registry: Registry<T>) {
    private val map = mutableMapOf<T, MutableSet<Motif>>()

    fun register(key: T, motif: Motif) {
        map.getOrPut(key) { mutableSetOf() } += motif
    }

    fun test(key: T): Set<Motif> = map.getOrElse(key) { setOf() }
}

fun <T> FairyDreamTable<T>.registerFromTag(tag: TagKey<T>, motif: Motif) {
    this.registry.getEntryList(tag).getOrNull()?.map { it.value() }.or { listOf() }.forEach {
        this.register(it, motif)
    }
}
