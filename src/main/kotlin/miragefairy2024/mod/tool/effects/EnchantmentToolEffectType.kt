package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.PoemType
import miragefairy2024.mod.text
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.max
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments

fun ToolConfiguration.enchantment(enchantment: Enchantment, level: Int = 1) = this.also {
    this.merge(EnchantmentToolEffectType, EnchantmentToolEffectType.Value(this, mapOf(enchantment to level)))
}

object EnchantmentToolEffectType : ToolEffectType<EnchantmentToolEffectType.Value> {
    class Value(val configuration: ToolConfiguration, val map: Map<Enchantment, Int>)

    override fun castOrThrow(value: Any) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.configuration, (a.map.keys + b.map.keys).associateWith { key -> (a.map[key] ?: 0) max (b.map[key] ?: 0) })
    override fun init(value: Value) {
        value.configuration.onAddPoemListeners += { _, poemList ->
            value.map.entries.fold(poemList) { poemList2, (enchantment, level) ->
                poemList2.text(PoemType.DESCRIPTION, text { translate(enchantment.translationKey) + if (level >= 2 || enchantment.maxLevel >= 2) " "() + level.toRomanText() else ""() })
            }
        }
        value.configuration.onOverrideEnchantmentLevelListeners += fail@{ _, enchantment, oldLevel ->
            val newLevel = value.map[enchantment] ?: return@fail oldLevel
            oldLevel max newLevel
        }
        value.configuration.onConvertItemStackListeners += { _, itemStack ->
            var itemStack2 = itemStack
            if ((value.map[Enchantments.SILK_TOUCH] ?: 0) >= 1) {
                itemStack2 = itemStack2.copy()
                val enchantments = EnchantmentHelper.get(itemStack2)
                enchantments[Enchantments.SILK_TOUCH] = (enchantments[Enchantments.SILK_TOUCH] ?: 0) atLeast 1
                EnchantmentHelper.set(enchantments, itemStack2)
            }
            itemStack2
        }
    }
}
