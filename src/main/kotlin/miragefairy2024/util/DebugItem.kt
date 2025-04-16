package miragefairy2024.util

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import mirrg.kotlin.hydrogen.toUpperCamelCase
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureMapping as TextureMap
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.world.level.Level as World
import java.io.File
import java.io.IOException

context(ModContext)
fun registerDebugItem(path: String, icon: Item = Items.BOOK, color: Int = 0x888888, action: (World, PlayerEntity, Hand, ItemStack) -> Unit) {
    val item = object : Item(Properties()) {
        override fun getName(stack: ItemStack) = text { path.toUpperCamelCase(afterDelimiter = " ")() }
        override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
            action(world, user, hand, user.getItemInHand(hand))
            return TypedActionResult.sidedSuccess(user.getItemInHand(hand), world.isClientSide)
        }
    }
    item.register(Registries.ITEM, MirageFairy2024.identifier(path))
    item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
    item.registerModelGeneration(Models.FLAT_ITEM) { TextureMap.layer0(icon) }
    item.registerColorProvider { _, _ -> color }
}

context(ModContext)
fun registerClientDebugItem(path: String, icon: Item = Items.BOOK, color: Int = 0x888888, action: (World, PlayerEntity, Hand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { world, player, hand, itemStack ->
        if (world.isServer) return@registerDebugItem
        action(world, player, hand, itemStack)
    }
}

context(ModContext)
fun registerServerDebugItem(path: String, icon: Item = Items.BOOK, color: Int = 0x888888, action: (ServerWorld, ServerPlayerEntity, Hand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { world, player, hand, itemStack ->
        if (world.isClientSide) return@registerDebugItem
        action(world as ServerWorld, player as ServerPlayerEntity, hand, itemStack)
    }
}

fun writeAction(player: PlayerEntity, fileName: String, text: String) {
    val file = File("debug").resolve(fileName)
    player.displayClientMessage(text { "Saved to "() + file() }, false)
    when {
        file.parentFile.isDirectory -> Unit
        file.parentFile.exists() -> throw IOException("Failed to create directory: $file")
        !file.parentFile.mkdirs() -> throw IOException("Failed to create directory: $file")
    }
    file.writeText(text)
}
