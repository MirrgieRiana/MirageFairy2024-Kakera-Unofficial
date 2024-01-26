package miragefairy2024.util

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import mirrg.kotlin.hydrogen.toUpperCamelCase
import net.minecraft.data.client.Models
import net.minecraft.data.client.TextureMap
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

fun registerDebugItem(path: String, icon: Item, color: Int, action: (World, PlayerEntity, Hand, ItemStack) -> Unit) {
    val item = object : Item(Settings()) {
        override fun getName(stack: ItemStack) = text { path.toUpperCamelCase(afterDelimiter = " ")() }
        override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
            action(world, user, hand, user.getStackInHand(hand))
            return TypedActionResult.success(user.getStackInHand(hand), world.isClient)
        }
    }
    item.register(Registries.ITEM, Identifier(MirageFairy2024.modId, path))
    item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
    item.registerItemModelGeneration(Models.GENERATED with TextureMap.layer0(icon))
    item.registerColorProvider { _, _ -> color }
}
