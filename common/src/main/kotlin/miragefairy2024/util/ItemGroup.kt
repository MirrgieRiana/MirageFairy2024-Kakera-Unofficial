package miragefairy2024.util

import miragefairy2024.ModContext
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.CreativeModeTab as ItemGroup

context(ModContext)
fun Registration<Item>.registerItemGroup(itemGroup: ResourceKey<ItemGroup>) {
    ItemGroupEvents.modifyEntriesEvent(itemGroup).register {
        it.accept(this())
    }
}

context(ModContext)
@Suppress("UnusedReceiverParameter")
fun Registration<Item>.registerItemGroup(itemGroup: ResourceKey<ItemGroup>, supplier: () -> List<ItemStack>) {
    ItemGroupEvents.modifyEntriesEvent(itemGroup).register {
        supplier().forEach { itemStack ->
            it.accept(itemStack)
        }
    }
}


class ItemGroupCard(
    val identifier: ResourceLocation,
    val enName: String,
    val jaName: String,
    icon: () -> ItemStack,
) {
    val translation = Translation({ "itemGroup.${identifier.toLanguageKey()}" }, enName, jaName)
    val itemGroupKey = Registries.CREATIVE_MODE_TAB with identifier
    val itemGroup: ItemGroup = FabricItemGroup.builder()
        .icon(icon)
        .title(text { translation() })
        .build()

    context(ModContext)
    fun init() {
        BuiltInRegistries.CREATIVE_MODE_TAB.register(identifier) { itemGroup }
        translation.enJa()
    }
}
