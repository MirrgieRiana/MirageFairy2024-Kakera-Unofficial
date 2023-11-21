package miragefairy2024.util.init

import mirrg.kotlin.hydrogen.unit
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

fun Item.registerItem(identifier: Identifier) = unit { Registry.register(Registries.ITEM, identifier, this) }