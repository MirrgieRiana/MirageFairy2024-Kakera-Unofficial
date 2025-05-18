package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.DynamicPoem
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.hydrogen.max
import mirrg.kotlin.hydrogen.or
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import kotlin.jvm.optionals.getOrNull

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
        value.map.forEach { (enchantment, level) ->
            configuration.descriptions += DynamicPoem(PoemType.DESCRIPTION) { context ->
                val trueEnchantment = context.registries().or { return@DynamicPoem Component.empty() }[Registries.ENCHANTMENT, enchantment].value()
                text { trueEnchantment.description + if (level >= 2 || trueEnchantment.maxLevel >= 2) " "() + level.toRomanText() else ""() }
            }
        }
        configuration.onOverrideEnchantmentLevelListeners += fail@{ _, enchantment, oldLevel ->
            val key = enchantment.unwrapKey().getOrNull() ?: return@fail oldLevel
            val newLevel = value.map[key] ?: return@fail oldLevel
            oldLevel max newLevel
        }
        configuration.onConvertItemStackListeners += { _, itemStack ->
            var itemStack2 = itemStack
            if ((value.map[Enchantments.SILK_TOUCH] ?: 0) >= 1) {
                itemStack2 = itemStack2.copy()
                val enchantments = itemStack2.get(DataComponents.ENCHANTMENTS)
                // TODO enchantments は Holder<Enchantment> を格納するが、この場所では RegistryKey<Enchantment> しか手に入らない
                // 根本的に処理方法を変える必要があるが、それはおそらくNeoForgeで変わる
                // enchantments[Enchantments.SILK_TOUCH] = (enchantments[Enchantments.SILK_TOUCH] ?: 0) atLeast 1
                FabricLoader::class.java.toString() // ←NeoForgeに移行したとき用のリマインダー
                itemStack2.set(DataComponents.ENCHANTMENTS, enchantments)
            }
            itemStack2
        }
    }
}
