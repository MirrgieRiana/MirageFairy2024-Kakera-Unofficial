package miragefairy2024.util

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import mirrg.kotlin.hydrogen.toUpperCamelCase
import net.minecraft.data.client.Models
import net.minecraft.data.client.TextureMap
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import java.io.File
import java.io.IOException

fun registerDebugItem(path: String, icon: Item = Items.BOOK, color: Int = 0x888888, action: (World, PlayerEntity, Hand, ItemStack) -> Unit) {
    val item = object : Item(Settings()) {
        override fun getName(stack: ItemStack) = text { path.toUpperCamelCase(afterDelimiter = " ")() }
        override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
            action(world, user, hand, user.getStackInHand(hand))
            return TypedActionResult.success(user.getStackInHand(hand), world.isClient)
        }
    }
    ModEvents.onRegistration {
        item.register(Registries.ITEM, Identifier(MirageFairy2024.modId, path))
    }
    ModEvents.onInitialize {
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        item.registerItemModelGeneration(Models.GENERATED with TextureMap.layer0(icon))
        item.registerColorProvider { _, _ -> color }
    }
}

fun registerClientDebugItem(path: String, icon: Item = Items.BOOK, color: Int = 0x888888, action: (World, PlayerEntity, Hand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { world, player, hand, itemStack ->
        if (!world.isClient) return@registerDebugItem
        action(world, player, hand, itemStack)
    }
}

fun registerServerDebugItem(path: String, icon: Item = Items.BOOK, color: Int = 0x888888, action: (ServerWorld, ServerPlayerEntity, Hand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { world, player, hand, itemStack ->
        if (world.isClient) return@registerDebugItem
        action(world as ServerWorld, player as ServerPlayerEntity, hand, itemStack)
    }
}

fun writeAction(player: PlayerEntity, fileName: String, text: String) {
    val file = File("debug").resolve(fileName)
    player.sendMessage(text { "Saved to "() + file() }, false)
    when {
        file.parentFile.isDirectory -> Unit
        file.parentFile.exists() -> throw IOException("Failed to create directory: $file")
        !file.parentFile.mkdirs() -> throw IOException("Failed to create directory: $file")
    }
    file.writeText(text)
}
