package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.max
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments

fun ToolConfiguration.enchantment(enchantment: ResourceKey<Enchantment>, level: Int = 1) = this.also {
    this.merge(EnchantmentToolEffectType, EnchantmentToolEffectType.Value(mapOf(enchantment to level))) { map ->
        EnchantmentToolEffectType.apply(this, map)
    }
}

object EnchantmentToolEffectType : ToolEffectType<EnchantmentToolEffectType.Value> {
    class Value(val map: Map<ResourceKey<Enchantment>, Int>)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value((a.map.keys + b.map.keys).associateWith { key -> (a.map[key] ?: 0) max (b.map[key] ?: 0) })

    fun apply(configuration: ToolConfiguration, value: Value) {
        if (value.map.isEmpty()) return
        value.map.entries.forEach { (enchantment, level) ->
            configuration.descriptions += text { enchantment.description + if (level >= 2 || enchantment.maxLevel >= 2) " "() + level.toRomanText() else ""() }
        }
        configuration.onOverrideEnchantmentLevelListeners += fail@{ _, enchantment, oldLevel ->
            val newLevel = value.map[enchantment] ?: return@fail oldLevel
            oldLevel max newLevel
        }
        configuration.onConvertItemStackListeners += { _, itemStack ->
            var itemStack2 = itemStack
            if ((value.map[Enchantments.SILK_TOUCH] ?: 0) >= 1) {
                itemStack2 = itemStack2.copy()
                val enchantments = EnchantmentHelper.getEnchantments(itemStack2)
                enchantments[Enchantments.SILK_TOUCH] = (enchantments[Enchantments.SILK_TOUCH] ?: 0) atLeast 1
                EnchantmentHelper.setEnchantments(enchantments, itemStack2)
            }
            itemStack2
        }
    }
}
