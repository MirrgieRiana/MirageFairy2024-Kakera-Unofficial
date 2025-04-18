package miragefairy2024.util

import miragefairy2024.ModContext
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.world.item.Item
import net.minecraft.world.item.CreativeModeTab as ItemGroup
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.resources.ResourceKey as RegistryKey
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.resources.ResourceLocation as Identifier

context(ModContext)
fun Item.registerItemGroup(itemGroup: RegistryKey<ItemGroup>) {
    ItemGroupEvents.modifyEntriesEvent(itemGroup).register {
        it.accept(this)
    }
}

context(ModContext)
@Suppress("UnusedReceiverParameter")
fun Item.registerItemGroup(itemGroup: RegistryKey<ItemGroup>, supplier: () -> List<ItemStack>) {
    ItemGroupEvents.modifyEntriesEvent(itemGroup).register {
        supplier().forEach { itemStack ->
            it.accept(itemStack)
        }
    }
}


class ItemGroupCard(
    val identifier: Identifier,
    val enName: String,
    val jaName: String,
    icon: () -> ItemStack,
) {
    val translation = Translation({ "itemGroup.${identifier.toLanguageKey()}" }, enName, jaName)
    val itemGroupKey = RegistryKeys.CREATIVE_MODE_TAB with identifier
    val itemGroup: ItemGroup = FabricItemGroup.builder()
        .icon(icon)
        .title(text { translation() })
        .build()

    context(ModContext)
    fun init() {
        itemGroup.register(Registries.CREATIVE_MODE_TAB, identifier)
        translation.enJa()
    }
}
