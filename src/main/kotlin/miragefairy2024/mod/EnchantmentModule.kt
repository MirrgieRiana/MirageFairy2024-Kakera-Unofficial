package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.ItemFilteringEnchantment
import miragefairy2024.mod.tool.items.ShootingStaffItem
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.register
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries

enum class EnchantmentCard(
    path: String,
    val en: String,
    val ja: String,
    val enchantment: Enchantment,
) {
    MAGIC_POWER("magic_power", "Magic Power", "魔法ダメージ増加", SimpleEnchantment(Enchantment.Rarity.COMMON, 5, 1, 10, 30) { it.item is ShootingStaffItem }),
    MAGIC_REACH("magic_reach", "Magic Reach", "魔法射程増加", SimpleEnchantment(Enchantment.Rarity.COMMON, 5, 1, 10, 30) { it.item is ShootingStaffItem }),
    MAGIC_ACCELERATION("magic_acceleration", "Magic Acceleration", "魔法加速", SimpleEnchantment(Enchantment.Rarity.COMMON, 5, 1, 10, 30) { it.item is ShootingStaffItem }),
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
}

class SimpleEnchantment(
    rarity: Rarity,
    private val maxLevel: Int,
    private val basePower: Int,
    private val powerPerLevel: Int,
    private val powerRange: Int,
    private val predicate: (ItemStack) -> Boolean,
) : Enchantment(rarity, EnchantmentTarget.VANISHABLE, arrayOf(EquipmentSlot.MAINHAND)), ItemFilteringEnchantment {
    override fun getMaxLevel() = maxLevel
    override fun getMinPower(level: Int) = basePower + (level - 1) * powerPerLevel
    override fun getMaxPower(level: Int) = super.getMinPower(level) + powerRange
    override fun isAcceptableItem(stack: ItemStack) = predicate(stack)
    override fun isAcceptableItemOnEnchanting(itemStack: ItemStack) = isAcceptableItem(itemStack)
}
