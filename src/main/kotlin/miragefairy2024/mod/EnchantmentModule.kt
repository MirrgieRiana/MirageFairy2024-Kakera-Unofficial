package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.ItemFilteringEnchantment
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.items.ScytheItem
import miragefairy2024.mod.tool.items.ShootingStaffItem
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.register
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.EnchantmentCategory as EnchantmentTarget
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries as Registries

enum class EnchantmentCard(
    path: String,
    val en: String,
    val ja: String,
    val enchantment: Enchantment,
) {
    MAGIC_POWER("magic_power", "Magic Power", "魔法ダメージ増加", SimpleEnchantment(Enchantment.Rarity.COMMON, 5, 1, 10, 30) { it.item is ShootingStaffItem }),
    MAGIC_REACH("magic_reach", "Magic Reach", "魔法射程増加", SimpleEnchantment(Enchantment.Rarity.COMMON, 5, 1, 10, 30) { it.item is ShootingStaffItem }),
    MAGIC_ACCELERATION("magic_acceleration", "Magic Acceleration", "魔法加速", SimpleEnchantment(Enchantment.Rarity.COMMON, 5, 1, 10, 30) { it.item is ShootingStaffItem }),
    FERTILITY("fertility", "Fertility", "豊穣", SimpleEnchantment(Enchantment.Rarity.RARE, 3, 15, 9, 50) { it.item is ScytheItem }),
    FORTUNE_UP("fortune_up", "Fortune Up", "幸運強化", SimpleEnchantment(Enchantment.Rarity.RARE, 3, 25, 25, 50, isTreasure = true) { false })
    ;

    val identifier = MirageFairy2024.identifier(path)
}

context(ModContext)
fun initEnchantmentModule() {
    EnchantmentCard.entries.forEach { card ->
        card.enchantment.register(Registries.ENCHANTMENT, card.identifier)
        en { card.enchantment.translationKey to card.en }
        ja { card.enchantment.translationKey to card.ja }
    }
    OverrideEnchantmentLevelCallback.EVENT.register { enchantment, itemStack, oldLevel ->
        if (enchantment != Enchantments.FORTUNE) return@register oldLevel
        if (oldLevel == 0) return@register 0
        oldLevel + EnchantmentHelper.getLevel(EnchantmentCard.FORTUNE_UP.enchantment, itemStack)
    }
}

class SimpleEnchantment(
    rarity: Rarity,
    private val maxLevel: Int,
    private val basePower: Int,
    private val powerPerLevel: Int,
    private val powerRange: Int,
    private val isTreasure: Boolean = false,
    private val predicate: (ItemStack) -> Boolean,
) : Enchantment(rarity, EnchantmentTarget.VANISHABLE, arrayOf(EquipmentSlot.MAINHAND)), ItemFilteringEnchantment {
    override fun getMaxLevel() = maxLevel
    override fun getMinPower(level: Int) = basePower + (level - 1) * powerPerLevel
    override fun getMaxPower(level: Int) = super.getMinPower(level) + powerRange
    override fun isAcceptableItem(stack: ItemStack) = predicate(stack)
    override fun isAcceptableItemOnEnchanting(itemStack: ItemStack) = isAcceptableItem(itemStack)
    override fun isTreasure() = isTreasure
}
