package miragefairy2024.util

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import mirrg.kotlin.hydrogen.toUpperCamelCase
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import java.io.File
import java.io.IOException
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult

fun interface TextureSource {
    fun getTextureMapping(): TextureMapping
}

fun Item.toTextureSource() = TextureSource { TextureMapping.layer0(this) }
fun Block.toTextureSource() = TextureSource { TextureMapping.layer0(this) }
fun ResourceLocation.toTextureSource() = TextureSource { TextureMapping.layer0(this) }

context(ModContext)
fun registerDebugItem(path: String, icon: TextureSource = Items.BOOK.toTextureSource(), color: Int = 0xFF888888.toInt(), action: (Level, Player, Hand, ItemStack) -> Unit) {
    val item = Registration(BuiltInRegistries.ITEM, MirageFairy2024.identifier(path)) {
        object : Item(Properties()) {
            override fun getName(stack: ItemStack) = text { path.toUpperCamelCase(afterDelimiter = " ")() }
            override fun use(world: Level, user: Player, hand: Hand): TypedActionResult<ItemStack> {
                action(world, user, hand, user.getItemInHand(hand))
                return TypedActionResult.sidedSuccess(user.getItemInHand(hand), world.isClientSide)
            }
        }
    }
    item.register()
    item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
    item.registerModelGeneration(Models.FLAT_ITEM) { icon.getTextureMapping() }
    item.registerColorProvider { _, _ -> color }
}

context(ModContext)
fun registerClientDebugItem(path: String, icon: TextureSource = Items.BOOK.toTextureSource(), color: Int = 0xFF888888.toInt(), action: (Level, Player, Hand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { world, player, hand, itemStack ->
        if (world.isServer) return@registerDebugItem
        action(world, player, hand, itemStack)
    }
}

context(ModContext)
fun registerServerDebugItem(path: String, icon: TextureSource = Items.BOOK.toTextureSource(), color: Int = 0xFF888888.toInt(), action: (ServerWorld, ServerPlayerEntity, Hand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { world, player, hand, itemStack ->
        if (world.isClientSide) return@registerDebugItem
        action(world as ServerWorld, player as ServerPlayerEntity, hand, itemStack)
    }
}

fun writeAction(player: Player, fileName: String, text: String) {
    val file = File("debug").resolve(fileName)
    player.displayClientMessage(text { "Saved to "() + file() }, false)
    when {
        file.parentFile.isDirectory -> Unit
        file.parentFile.exists() -> throw IOException("Failed to create directory: $file")
        !file.parentFile.mkdirs() -> throw IOException("Failed to create directory: $file")
    }
    file.writeText(text)
}
