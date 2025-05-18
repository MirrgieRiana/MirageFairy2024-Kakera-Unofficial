package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.util.EnJa
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerEnchantmentTagGeneration
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.enchantment.ItemEnchantments

val MAGIC_WEAPON_ITEM_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, MirageFairy2024.identifier("magic_weapon"))
val SCYTHE_ITEM_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, MirageFairy2024.identifier("scythe"))
val NONE_ITEM_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, MirageFairy2024.identifier("none"))

enum class EnchantmentRarity(val weight: Int, val anvilCost: Int) {
    COMMON(10, 1),
    UNCOMMON(5, 2),
    RARE(2, 4),
    VERY_RARE(1, 8),
}

enum class EnchantmentCard(
    path: String,
    private val description: EnJa,
    private val targetItemTag: TagKey<Item>,
    private val primaryItemTag: TagKey<Item>,
    private val rarity: EnchantmentRarity,
    private val maxLevel: Int,
    private val basePower: Int,
    private val powerPerLevel: Int,
    private val powerRange: Int,
    private val tags: List<TagKey<Enchantment>> = listOf(),
) {
    MAGIC_POWER(
        "magic_power", EnJa("Magic Power", "魔法ダメージ増加"),
        MAGIC_WEAPON_ITEM_TAG, MAGIC_WEAPON_ITEM_TAG, EnchantmentRarity.COMMON,
        5, 1, 10, 30,
        tags = listOf(EnchantmentTags.NON_TREASURE),
    ),
    MAGIC_REACH(
        "magic_reach", EnJa("Magic Reach", "魔法射程増加"),
        MAGIC_WEAPON_ITEM_TAG, MAGIC_WEAPON_ITEM_TAG, EnchantmentRarity.COMMON,
        5, 1, 10, 30,
        tags = listOf(EnchantmentTags.NON_TREASURE),
    ),
    MAGIC_ACCELERATION(
        "magic_acceleration", EnJa("Magic Acceleration", "魔法加速"),
        MAGIC_WEAPON_ITEM_TAG, MAGIC_WEAPON_ITEM_TAG, EnchantmentRarity.COMMON,
        5, 1, 10, 30,
        tags = listOf(EnchantmentTags.NON_TREASURE),
    ),
    FORTUNE_UP(
        "fortune_up", EnJa("Fortune Up", "幸運強化"),
        NONE_ITEM_TAG, NONE_ITEM_TAG, EnchantmentRarity.RARE,
        3, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val key: ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, identifier)

    context(ModContext)
    fun init() {
        registerDynamicGeneration(key) {
            Enchantment.enchantment(
                Enchantment.definition(
                    lookup(Registries.ITEM).getOrThrow(targetItemTag),
                    lookup(Registries.ITEM).getOrThrow(primaryItemTag),
                    rarity.weight,
                    maxLevel,
                    Enchantment.dynamicCost(basePower, powerPerLevel),
                    Enchantment.dynamicCost(basePower + powerRange, powerPerLevel),
                    rarity.anvilCost,
                    EquipmentSlotGroup.MAINHAND,
                )
            )
                .build(identifier)
        }
        en { identifier.toLanguageKey("enchantment") to description.en }
        ja { identifier.toLanguageKey("enchantment") to description.ja }
        tags.forEach {
            key.registerEnchantmentTagGeneration { it }
        }
    }
}

context(ModContext)
fun initEnchantmentModule() {
    EnchantmentCard.entries.forEach { card ->
        card.init()
    }

    OverrideEnchantmentLevelCallback.EVENT.register { enchantment, itemStack, oldLevel ->
        if (!enchantment.`is`(Enchantments.FORTUNE)) return@register oldLevel
        if (oldLevel == 0) return@register 0

        val itemEnchantments = itemStack.get(DataComponents.ENCHANTMENTS) ?: ItemEnchantments.EMPTY
        val entry = itemEnchantments.entrySet().firstOrNull { it.key.`is`(EnchantmentCard.FORTUNE_UP.key) } ?: return@register oldLevel

        oldLevel + entry.intValue
    }

    SCYTHE_ITEM_TAG.registerItemTagGeneration { ItemTags.MINING_LOOT_ENCHANTABLE }
}
