package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.createItemStack
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

val mirageFairy2024ItemGroup: RegistryKey<ItemGroup> = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier(MirageFairy2024.modId, "miragefairy2024"))

fun initCommonModule() {

    val itemGroup = FabricItemGroup.builder()
        .icon { MaterialCard.FAIRY_PLASTIC.item.createItemStack() }
        .displayName(text { translate("itemGroup.mirageFairy2024") })
        .build()
    itemGroup.register(Registries.ITEM_GROUP, mirageFairy2024ItemGroup.value)

    en { "itemGroup.mirageFairy2024" to "MirageFairy2024" }
    ja { "itemGroup.mirageFairy2024" to "MirageFairy2024" }

}
