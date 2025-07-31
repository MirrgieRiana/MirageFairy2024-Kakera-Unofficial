package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.item.MaterialCard
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.toIngredient
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.Tiers
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.Tier as ToolMaterial
import net.minecraft.world.item.Tiers as ToolMaterials

enum class FairyToolMaterials(
    private val miningLevel: Tiers,
    private val durability: Int,
    private val miningSpeedMultiplier: Float,
    private val attackDamage: Float,
    val axeAttackDamage: Float,
    private val enchantability: Int,
    private val repairIngredient: () -> Ingredient,
) : ToolMaterial {
    COPPER(Tiers.IRON, 196, 5.0F, 1.0F, 2.0F, 18, { Items.COPPER_INGOT.toIngredient() }),
    GLASS(Tiers.STONE, 27, 6.0F, 4.0F, 3.5F, 14, { Items.GLASS.toIngredient() }),
    AMETHYST(Tiers.IRON, 218, 5.5F, 2.0F, 2.5F, 23, { Items.AMETHYST_SHARD.toIngredient() }),
    OBSIDIAN(Tiers.IRON, 804, 5.0F, 4.5F, 3.0F, 20, { Items.OBSIDIAN.toIngredient() }),
    EMERALD(Tiers.DIAMOND, 1192, 7.0F, 2.5F, 2.0F, 16, { Items.EMERALD.toIngredient() }),
    ECHO_SHARD(Tiers.NETHERITE, 1366, 12.0F, 4.0F, 3.0F, 12, { Items.ECHO_SHARD.toIngredient() }),
    NETHER_STAR(Tiers.NETHERITE, 5048, 11.0F, 5.0F, 3.5F, 25, { Items.NETHER_STAR.toIngredient() }),

    MAGNETITE(Tiers.STONE, 220, 5.0F, 1.5F, 2.0F, 3, { MaterialCard.MAGNETITE.item().toIngredient() }),
    BISMUTH(Tiers.IRON, 16, 4.0F, 1.0F, 1.5F, 19, { MaterialCard.BISMUTH_INGOT.item().toIngredient() }),
    FLUORITE(Tiers.STONE, 96, 3.0F, 1.0F, 1.5F, 20, { MaterialCard.FLUORITE.item().toIngredient() }),
    TOPAZ(Tiers.DIAMOND, 1285, 6.5F, 4.0F, 1.5F, 12, { MaterialCard.TOPAZ.item().toIngredient() }),
    FLINT(Tiers.STONE, 150, 3.5F, 1.5F, 1.0F, 6, { Items.FLINT.toIngredient() }),

    MIRAGE(Tiers.WOOD, 48, 1.6F, 0.0F, 0.0F, 17, { MaterialCard.MIRAGE_STEM.item().toIngredient() }),
    MIRAGIUM(Tiers.IRON, 478, 1.0F, 2.0F, 0.0F, 26, { MaterialCard.MIRAGIUM_INGOT.item().toIngredient() }),
    LILAGIUM(Tiers.IRON, 505, 1.0F, 2.0F, 0.0F, 19, { MaterialCard.LILAGIUM_INGOT.item().toIngredient() }),
    MIRAGIDIAN(Tiers.NETHERITE, 7826, 7.0F, 2.5F, 1.5F, 2, { MaterialCard.MIRAGIDIAN.item().toIngredient() }),
    FAIRY_CRYSTAL(Tiers.IRON, 235, 5.0F, 1.5F, 1.5F, 7, { MaterialCard.FAIRY_CRYSTAL.item().toIngredient() }),
    PHANTOM_DROP(Tiers.NETHERITE, 777, 9.0F, 2.0F, 1.0F, 12, { MaterialCard.PHANTOM_DROP.item().toIngredient() }),
    LUMINITE(Tiers.DIAMOND, 1361, 9.0F, 4.0F, 3.0F, 21, { MaterialCard.LUMINITE.item().toIngredient() }),
    RESONITE(Tiers.NETHERITE, 2705, 4.0F, 9.0F, 7.0F, 19, { MaterialCard.RESONITE_INGOT.item().toIngredient() }),
    PROMINITE(Tiers.DIAMOND, 925, 7.0F, 2.0F, 0.0F, 13, { MaterialCard.PROMINITE.item().toIngredient() }),
    XARPITE(Tiers.IRON, 283, 1.0F, 2.0F, 2.0F, 20, { MaterialCard.XARPITE.item().toIngredient() }),
    MIRANAGITE(Tiers.IRON, 256, 6.5F, 2.0F, 2.5F, 24, { MaterialCard.MIRANAGITE.item().toIngredient() }),
    CHAOS_STONE(Tiers.NETHERITE, 666, 2.0F, 2.0F, 2.0F, 15, { MaterialCard.CHAOS_STONE.item().toIngredient() }),
    HAIMEVISKA_ROSIN(Tiers.WOOD, 73, 0.5F, 0.0F, 0.0F, 11, { MaterialCard.HAIMEVISKA_ROSIN.item().toIngredient() }),

    NEUTRONIUM(Tiers.NETHERITE, Int.MAX_VALUE - 100, 8.0F, 3.0F, 3.0F, 10, { Items.BEDROCK.toIngredient() }),
    ;

    override fun getUses() = durability
    override fun getSpeed() = miningSpeedMultiplier
    override fun getAttackDamageBonus() = attackDamage
    override fun getIncorrectBlocksForDrops(): TagKey<Block> = miningLevel.incorrectBlocksForDrops
    override fun getEnchantmentValue() = enchantability
    override fun getRepairIngredient() = repairIngredient()
}

val ToolMaterial.axeAttackDamageBonus
    get() = when (this) {
        ToolMaterials.WOOD -> 0.0F
        ToolMaterials.STONE -> 2.0F
        ToolMaterials.IRON -> 2.0F
        ToolMaterials.DIAMOND -> 2.0F
        ToolMaterials.GOLD -> 0.0F
        ToolMaterials.NETHERITE -> 3.0F
        is FairyToolMaterials -> this.axeAttackDamage
        else -> throw IllegalArgumentException("Unsupported tool material: $this")
    }

enum class ToolMaterialCard(val toolMaterial: ToolMaterial, path: String, enName: String, jaName: String) {
    WOOD(ToolMaterials.WOOD, "wooden_tool", "Wooden Tool", "木ツール"),
    STONE(ToolMaterials.STONE, "stone_tool", "Stone Tool", "石ツール"),
    IRON(ToolMaterials.IRON, "iron_tool", "Iron Tool", "鉄ツール"),
    GOLD(ToolMaterials.GOLD, "golden_tool", "Golden Tool", "金ツール"),
    DIAMOND(ToolMaterials.DIAMOND, "diamond_tool", "Diamond Tool", "ダイヤモンドツール"),
    NETHERITE(ToolMaterials.NETHERITE, "netherite_tool", "Netherite Tool", "ネザライトツール"),

    COPPER(FairyToolMaterials.COPPER, "copper_tool", "Copper Tool", "銅ツール"),
    GLASS(FairyToolMaterials.GLASS, "glass_tool", "Glass Tool", "ガラスツール"),
    AMETHYST(FairyToolMaterials.AMETHYST, "amethyst_tool", "Amethyst Tool", "アメジストツール"),
    OBSIDIAN(FairyToolMaterials.OBSIDIAN, "obsidian_tool", "Obsidian Tool", "黒曜石ツール"),
    EMERALD(FairyToolMaterials.EMERALD, "emerald_tool", "Emerald Tool", "エメラルドツール"),
    ECHO_SHARD(FairyToolMaterials.ECHO_SHARD, "echo_shard_tool", "Echo Shard Tool", "残響ツール"),
    NETHER_STAR(FairyToolMaterials.NETHER_STAR, "nether_star_tool", "Nether Star Tool", "ネザースターツール"),

    MAGNETITE(FairyToolMaterials.MAGNETITE, "magnetite_tool", "Magnetite Tool", "磁鉄鉱ツール"),
    BISMUTH(FairyToolMaterials.BISMUTH, "bismuth_tool", "Bismuth Tool", "ビスマスツール"),
    FLUORITE(FairyToolMaterials.FLUORITE, "fluorite_tool", "Fluorite Tool", "蛍石ツール"),
    TOPAZ(FairyToolMaterials.TOPAZ, "topaz_tool", "Topaz Tool", "トパーズツール"),
    FLINT(FairyToolMaterials.FLINT, "flint_tool", "Flint Tool", "火打石ツール"),

    MIRAGE(FairyToolMaterials.MIRAGE, "mirage_tool", "Mirage Tool", "ミラージュツール"), // TODO 用途
    MIRAGIUM(FairyToolMaterials.MIRAGIUM, "miragium_tool", "Miragium Tool", "ミラジウムツール"),
    LILAGIUM(FairyToolMaterials.LILAGIUM, "lilagium_tool", "Lilagium Tool", "リラジウムツール"),
    MIRAGIDIAN(FairyToolMaterials.MIRAGIDIAN, "miragidian_tool", "Miragidian Tool", "ミラジディアンツール"),
    FAIRY_CRYSTAL(FairyToolMaterials.FAIRY_CRYSTAL, "fairy_crystal_tool", "Fairy Crystal", "フェアリークリスタルツール"),
    PHANTOM_DROP(FairyToolMaterials.PHANTOM_DROP, "phantom_tool", "Phantom Tool", "幻想ツール"),
    LUMINITE(FairyToolMaterials.LUMINITE, "luminite_tool", "Luminite Tool", "ルミナイトツール"),
    RESONITE(FairyToolMaterials.RESONITE, "resonite_tool", "Resonance Tool", "共鳴ツール"),
    PROMINITE(FairyToolMaterials.PROMINITE, "prominite_tool", "Prominite Tool", "プロミナイトツール"),
    XARPITE(FairyToolMaterials.XARPITE, "xarpite_tool", "Xarpite Tool", "紅天石ツール"),
    MIRANAGITE(FairyToolMaterials.MIRANAGITE, "miranagite_tool", "Miranagite Tool", "蒼天石ツール"),
    CHAOS_STONE(FairyToolMaterials.CHAOS_STONE, "chaos_tool", "Chaos Tool", "混沌ツール"),
    HAIMEVISKA_ROSIN(FairyToolMaterials.HAIMEVISKA_ROSIN, "haimeviska_rosin_tool", "Rosin Tool", "涙ツール"),

    NEUTRONIUM(FairyToolMaterials.NEUTRONIUM, "neutronium_tool", "Neutronium Tool", "ニュートロニウムツール")
    ;

    val identifier = MirageFairy2024.identifier(path)
    val tag: TagKey<Item> = TagKey.create(Registries.ITEM, identifier)
    val translation = Translation({ "${MirageFairy2024.MOD_ID}.tool_material.$path" }, enName, jaName)
}

context(ModContext)
fun initToolMaterial() {

    ToolMaterialCard.entries.forEach { card ->
        card.translation.enJa()
    }


    fun register(card: ToolMaterialCard, item: () -> Item) = item.registerItemTagGeneration { card.tag }

    // WOOD
    register(ToolMaterialCard.WOOD) { Items.WOODEN_SWORD }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_SHOVEL }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_PICKAXE }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_AXE }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_HOE }
    register(ToolMaterialCard.WOOD) { Items.BOW }
    register(ToolMaterialCard.WOOD) { Items.CROSSBOW }
    register(ToolMaterialCard.WOOD) { Items.FISHING_ROD }
    register(ToolMaterialCard.WOOD) { Items.CARROT_ON_A_STICK }
    register(ToolMaterialCard.WOOD) { Items.WARPED_FUNGUS_ON_A_STICK }

    // STONE
    register(ToolMaterialCard.STONE) { Items.STONE_SWORD }
    register(ToolMaterialCard.STONE) { Items.STONE_SHOVEL }
    register(ToolMaterialCard.STONE) { Items.STONE_PICKAXE }
    register(ToolMaterialCard.STONE) { Items.STONE_AXE }
    register(ToolMaterialCard.STONE) { Items.STONE_HOE }

    // IRON
    register(ToolMaterialCard.IRON) { Items.IRON_SWORD }
    register(ToolMaterialCard.IRON) { Items.IRON_SHOVEL }
    register(ToolMaterialCard.IRON) { Items.IRON_PICKAXE }
    register(ToolMaterialCard.IRON) { Items.IRON_AXE }
    register(ToolMaterialCard.IRON) { Items.IRON_HOE }
    register(ToolMaterialCard.IRON) { Items.FLINT_AND_STEEL }
    register(ToolMaterialCard.IRON) { Items.SHEARS }

    // COPPER
    register(ToolMaterialCard.COPPER) { Items.SPYGLASS }
    register(ToolMaterialCard.COPPER) { Items.BRUSH }
    register(ToolMaterialCard.COPPER) { Items.TRIDENT }

    // GOLD
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_SWORD }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_SHOVEL }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_PICKAXE }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_AXE }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_HOE }

    // DIAMOND
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_SWORD }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_SHOVEL }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_PICKAXE }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_AXE }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_HOE }

    // NETHERITE
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_SWORD }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_SHOVEL }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_PICKAXE }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_AXE }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_HOE }

}
