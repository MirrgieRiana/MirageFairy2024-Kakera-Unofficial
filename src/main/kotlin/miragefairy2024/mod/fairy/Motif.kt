package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.FoodIngredientCategoryCard
import miragefairy2024.mod.ToolMaterialCard
import miragefairy2024.mod.passiveskill.CategoryFoodIngredientPassiveSkillCondition
import miragefairy2024.mod.passiveskill.DoubleComparisonPassiveSkillCondition
import miragefairy2024.mod.passiveskill.EntityAttributePassiveSkillEffect
import miragefairy2024.mod.passiveskill.IntComparisonPassiveSkillCondition
import miragefairy2024.mod.passiveskill.ItemFoodIngredientPassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillEffectCard
import miragefairy2024.mod.passiveskill.PassiveSkillSpecification
import miragefairy2024.mod.passiveskill.SimplePassiveSkillConditionCard
import miragefairy2024.mod.passiveskill.StatusEffectPassiveSkillEffect
import miragefairy2024.mod.passiveskill.ToolMaterialCardPassiveSkillCondition
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.registerDebugItem
import miragefairy2024.util.writeAction
import mirrg.kotlin.hydrogen.join
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

val motifRegistryKey: RegistryKey<Registry<Motif>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "motif"))
val motifRegistry: Registry<Motif> = FabricRegistryBuilder.createSimple(motifRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

fun Motif.getIdentifier() = motifRegistry.getId(this)
fun Identifier.toFairyMotif() = motifRegistry.get(this)

interface Motif {
    val displayName: Text
    val skinColor: Int
    val frontColor: Int
    val backColor: Int
    val hairColor: Int
    val rare: Int
    val passiveSkillSpecifications: List<PassiveSkillSpecification<*>>
}


// TODO 妖精の系統関係
enum class MotifCard(
    path: String,
    override val rare: Int,
    enName: String,
    jaName: String,
    override val skinColor: Int,
    override val frontColor: Int,
    override val backColor: Int,
    override val hairColor: Int,
    passiveSkillBuilder: PassiveSkillBuilder,
    val recipes: MotifCardRecipes,
) : Motif {
    AIR(
        "air", 0, "Airia", "空気精アイリャ", 0xFFBE80, 0xDEFFFF, 0xDEFFFF, 0xB0FFFF,
        PassiveSkillBuilder() + speed(1.0),
        MotifCardRecipes().always + Blocks.AIR,
    ),
    LIGHT(
        "light", 3, "Lightia", "光精リグチャ", 0xFFFFD8, 0xFFFFD8, 0xFFFFC5, 0xFFFF00,
        PassiveSkillBuilder()
            + speed(0.4) * light.atLeast(15)
            + speed(0.4) * light.atLeast(10)
            + speed(0.4) * light.atLeast(5),
        MotifCardRecipes().overworld,
    ),
    VACUUM_DECAY(
        "vacuum_decay", 13, "Vacuume Decia", "真空崩壊精ヴァツーメデーツャ", 0x00003B, 0x000012, 0x000012, 0x000078,
        PassiveSkillBuilder()
            + StatusEffects.STRENGTH(2)
            + attack(0.5)
            + StatusEffects.WITHER() // TODO 真空浸蝕：死ぬまで徐々にダメージ、近接攻撃時に感染
            + StatusEffects.UNLUCK(3), // TODO MOBドロップを減らす効果
        MotifCardRecipes().end + BlockMaterialCard.LOCAL_VACUUM_DECAY.block,
    ),
    SUN(
        "sun", 10, "Sunia", "太陽精スーニャ", 0xff2f00, 0xff972b, 0xff7500, 0xffe7b2,
        PassiveSkillBuilder()
            + attack(2.0) * overworld * daytime * fine * skyVisible
            + regeneration(1.0) * overworld * daytime * fine * skyVisible,
        MotifCardRecipes().overworld,
    ),
    FIRE(
        "fire", 2, "Firia", "火精フィーリャ", 0xFF6C01, 0xF9DFA4, 0xFF7324, 0xFF4000,
        PassiveSkillBuilder()
            + attack(1.5) * onFire
            + ignition * health.atLeast(6.0),
        MotifCardRecipes().nether + Blocks.FIRE,
    ),
    WATER(
        "water", 1, "Wateria", "水精ワテーリャ", 0x5469F2, 0x5985FF, 0x172AD3, 0x2D40F4,
        PassiveSkillBuilder()
            + health(0.5) * underwater
            + regeneration(1.0) * underwater, // TODO ネザー以外で消火効果
        MotifCardRecipes().overworld + Blocks.WATER,
    ),
    DIRT(
        "dirt", 1, "Dirtia", "土精ディルチャ", 0xB87440, 0xB9855C, 0x593D29, 0x914A18,
        PassiveSkillBuilder()
            + health(1.0) * overworld
            + regeneration(0.2) * overworld,
        MotifCardRecipes().overworld + BlockTags.DIRT,
    ),
    STONE(
        "stone", 2, "Stonia", "石精ストーニャ", 0x333333, 0x8F8F8F, 0x686868, 0x747474,
        PassiveSkillBuilder()
            + health(0.6)
            + attack(0.4)
            + StatusEffects.RESISTANCE() * ToolMaterialCard.STONE()
            + StatusEffects.RESISTANCE(2) * ToolMaterialCard.STONE() * fairyLevel.atLeast(14.0),
        MotifCardRecipes().overworld + Blocks.STONE,
    ),
    COPPER(
        "copper", 3, "Copperia", "銅精ツォッペーリャ", 0xF69D7F, 0xF77653, 0xF77653, 0x5DC09A,
        PassiveSkillBuilder()
            + luck(0.6)
            + health(0.4)
            + StatusEffects.RESISTANCE() * ToolMaterialCard.COPPER() // TODO 魔法？電気？にちなんだステータス効果
            + StatusEffects.RESISTANCE(2) * ToolMaterialCard.COPPER() * fairyLevel.atLeast(10.0),
        MotifCardRecipes().overworld + Blocks.COPPER_BLOCK + Items.COPPER_INGOT,
    ),
    IRON(
        "iron", 4, "Ironia", "鉄精イローニャ", 0xA0A0A0, 0xD8D8D8, 0x727272, 0xD8AF93,
        PassiveSkillBuilder()
            + attack(0.6)
            + health(0.4)
            + StatusEffects.STRENGTH() * ToolMaterialCard.IRON()
            + StatusEffects.STRENGTH(2) * ToolMaterialCard.IRON() * fairyLevel.atLeast(10.0),
        MotifCardRecipes().overworld + Blocks.IRON_BLOCK + Items.IRON_INGOT,
    ),
    GOLD(
        "gold", 6, "Goldia", "金精ゴルジャ", 0xEFE642, 0xF4CC17, 0xF4CC17, 0xFDB61E,
        PassiveSkillBuilder()
            + luck(0.8)
            + health(0.2)
            + StatusEffects.LUCK() * ToolMaterialCard.GOLD()
            + StatusEffects.LUCK(2) * ToolMaterialCard.GOLD() * fairyLevel.atLeast(12.0),
        MotifCardRecipes().overworld.nether + Blocks.GOLD_BLOCK + Items.GOLD_INGOT,
    ),
    NETHERITE(
        "netherite", 9, "Netheritia", "地獄合金精ネテリーチャ", 0x8F788F, 0x74585B, 0x705558, 0x77302D,
        PassiveSkillBuilder()
            + attack(0.6)
            + luck(0.4)
            + StatusEffects.FIRE_RESISTANCE() * ToolMaterialCard.NETHERITE()
            + StatusEffects.STRENGTH(2) * ToolMaterialCard.NETHERITE() * fairyLevel.atLeast(16.0),
        MotifCardRecipes() + Blocks.NETHERITE_BLOCK + Items.NETHERITE_INGOT,
    ),
    DIAMOND(
        "diamond", 7, "Diamondia", "金剛石精ディアモンジャ", 0x97FFE3, 0xD1FAF3, 0x70FFD9, 0x30DBBD,
        PassiveSkillBuilder()
            + luck(0.8)
            + attack(0.2)
            + StatusEffects.HASTE() * ToolMaterialCard.DIAMOND()
            + StatusEffects.HASTE(2) * ToolMaterialCard.DIAMOND() * fairyLevel.atLeast(16.0),
        MotifCardRecipes().overworld + Blocks.DIAMOND_BLOCK + Items.DIAMOND,
    ),
    PIG(
        "pig", 2, "Pigia", "豚精ピーギャ", 0xDB98A2, 0xF68C87, 0xC76B73, 0xDC94A1,
        PassiveSkillBuilder()
            + health(0.8) * food(Items.PORKCHOP)
            + regeneration(0.1) * food(Items.CARROT)
            + regeneration(0.1) * food(Items.POTATO)
            + regeneration(0.1) * food(Items.BEETROOT)
            + health(0.4) * food.atLeast(12),
        MotifCardRecipes().overworld + EntityType.PIG,
    ),
    COW(
        "cow", 2, "Cowia", "牛精ツォーウャ", 0x433626, 0x644B37, 0x4A3828, 0xADADAD,
        PassiveSkillBuilder()
            + attack(0.8) * food(Items.BEEF)
            + StatusEffects.STRENGTH() * food(Items.WHEAT)
            + attack(0.4) * food.atLeast(12),
        MotifCardRecipes().overworld + EntityType.COW,
    ),
    CHICKEN(
        "chicken", 2, "Chickenia", "鶏精キッケーニャ", 0xF3DE71, 0xEDEDED, 0xEDEDED, 0xD93117,
        PassiveSkillBuilder()
            + StatusEffects.SLOW_FALLING() * food(Items.CHICKEN) * fairyLevel.atLeast(11.0)
            + regeneration(0.2) * food.atLeast(12),
        MotifCardRecipes().overworld + EntityType.CHICKEN,
    ),
    RABBIT(
        "rabbit", 5, "Rabbitia", "兎精ラッビーチャ", 0x9E866A, 0x8C7A64, 0x8C7962, 0x615345,
        PassiveSkillBuilder()
            + StatusEffects.JUMP_BOOST(1) * food(Items.RABBIT)
            + StatusEffects.JUMP_BOOST(2) * food(Items.RABBIT) * fairyLevel.atLeast(14.0)
            + StatusEffects.LUCK(1) * food(Items.CARROT)
            + StatusEffects.LUCK(2) * food(Items.CARROT) * fairyLevel.atLeast(11.0)
            + luck(0.5) * food.atLeast(12),
        MotifCardRecipes().overworld + EntityType.RABBIT,
    ),
    PLAYER(
        "player", 5, "Playeria", "人精プライェーリャ", 0xB58D63, 0x00AAAA, 0x322976, 0x4B3422,
        PassiveSkillBuilder() + experience(1.0) * level.atMost(29),
        MotifCardRecipes().always + EntityType.PLAYER,
    ),
    ENDERMAN(
        "enderman", 6, "Endermania", "終界人精エンデルマーニャ", 0x000000, 0x161616, 0x161616, 0xEF84FA,
        PassiveSkillBuilder() + collection(1.2) * food.atLeast(12),
        MotifCardRecipes().overworld.nether.end + EntityType.ENDERMAN,
    ),
    WITHER(
        "wither", 8, "Witheria", "枯精ウィテーリャ", 0x181818, 0x3C3C3C, 0x141414, 0x557272,
        PassiveSkillBuilder()
            + attack(1.0) * food.atMost(6) // TODO 遠距離
            + StatusEffects.SLOW_FALLING() * food.atMost(6)
            + StatusEffects.JUMP_BOOST() * food.atMost(6)
            + StatusEffects.JUMP_BOOST(2) * food.atMost(6) * fairyLevel.atLeast(10.0)
            + StatusEffects.SLOWNESS(2) * food.atMost(6) * fairyLevel.atMost(12.0)
            + StatusEffects.JUMP_BOOST(3) * food.atMost(6) * fairyLevel.atLeast(14.0)
            + StatusEffects.SLOWNESS() * food.atMost(6) * fairyLevel.atMost(16.0),
        MotifCardRecipes().nether + EntityType.WITHER,
    ),
    MUSHROOM(
        "mushroom", 3, "Mushroomia", "茸精ムシュローミャ", 0xDEDBD1, 0xC7C2AF, 0xC7C1AF, 0x8A836E,
        PassiveSkillBuilder()
            + health(0.2) * food(FoodIngredientCategoryCard.MUSHROOM)
            + regeneration(0.2) * food(FoodIngredientCategoryCard.MUSHROOM)
            + mana(1.0),
        MotifCardRecipes().overworld.nether,
    ),
    RED_MUSHROOM(
        "red_mushroom", 3, "Rede Mushroomia", "赤茸精レーデムシュローミャ", 0xE6DBA8, 0xFF0A0A, 0xFF0A0A, 0xBFD7D9,
        PassiveSkillBuilder()
            + StatusEffects.HEALTH_BOOST(1) * food(Items.RED_MUSHROOM)
            + StatusEffects.HEALTH_BOOST(2) * food(Items.RED_MUSHROOM) * fairyLevel.atLeast(10.0)
            + health(0.2) * food.atLeast(12),
        MotifCardRecipes().overworld.nether + Blocks.RED_MUSHROOM + Items.RED_MUSHROOM,
    ),
    BROWN_MUSHROOM(
        "brown_mushroom", 3, "Browne Mushroomia", "茶茸精ブロウネムシュローミャ", 0xDEB6A2, 0xF0AD8B, 0xC28C70, 0xDE9571,
        PassiveSkillBuilder()
            + regeneration(1.0) * food(Items.BROWN_MUSHROOM)
            + regeneration(0.2) * food.atLeast(12),
        MotifCardRecipes().overworld.nether + Blocks.BROWN_MUSHROOM + Items.BROWN_MUSHROOM,
    ),
    CARROT(
        "carrot", 4, "Carrotia", "人参精ツァッローチャ", 0xF98D10, 0xFD7F11, 0xE3710F, 0x248420,
        PassiveSkillBuilder()
            + StatusEffects.NIGHT_VISION(additionalSeconds = 10) * food(Items.CARROT)
            + regeneration(0.1) * fairyLevel.atLeast(10.0),
        MotifCardRecipes().overworld + Blocks.CARROTS + Items.CARROT,
    ),
    POTATO(
        "potato", 4, "Potatia", "芋精ポターチャ", 0xEAC278, 0xE7B456, 0xE7B456, 0x248420,
        PassiveSkillBuilder()
            + StatusEffects.STRENGTH(1) * food(Items.POTATO)
            + StatusEffects.STRENGTH(2) * food(Items.POTATO) * fairyLevel.atLeast(14.0)
            + regeneration(0.1) * food.atLeast(12),
        MotifCardRecipes().overworld + Blocks.POTATOES + Items.POTATO,
    ),
    MELON(
        "melon", 6, "Melonia", "西瓜精メローニャ", 0xFF5440, 0xA6EE63, 0x195612, 0x01A900,
        PassiveSkillBuilder()
            + experience(0.2) * level.atMost(29) * food(Items.MELON_SLICE)
            + regeneration(0.4) * food(Items.MELON_SLICE)
            + regeneration(0.4) * food.atLeast(12),
        MotifCardRecipes().common(ConventionalBiomeTags.JUNGLE) + Blocks.MELON + Items.MELON_SLICE,
    ),
    APPLE(
        "apple", 4, "Applia", "林檎精アップーリャ", 0xFF755D, 0xFF564E, 0xFF0000, 0x01A900,
        PassiveSkillBuilder()
            + experience(0.6) * level.atMost(29) * food(Items.APPLE)
            + regeneration(0.4) * food.atLeast(12),
        MotifCardRecipes().overworld + Items.APPLE,
    ),
    WOOD(
        "wood", 2, "Woodia", "木精ウォージャ", 0xE7C697, 0xAD8232, 0xAD8232, 0x8B591C,
        PassiveSkillBuilder()
            + attack(0.4)
            + health(0.2)
            + StatusEffects.SPEED() * ToolMaterialCard.WOOD() // TODO 射撃攻撃力増加
            + StatusEffects.SPEED(2) * ToolMaterialCard.WOOD() * fairyLevel.atLeast(12.0)
            + mending(1.0) * ToolMaterialCard.WOOD() * fairyLevel.atLeast(16.0),
        MotifCardRecipes().overworld + BlockTags.LOGS + BlockTags.PLANKS,
    ),
    CAKE(
        "cake", 4, "Cakia", "蛋麭精ツァーキャ", 0xCC850C, 0xF5F0DC, 0xD3D0BF, 0xDE3334,
        PassiveSkillBuilder() + mana(1.0), // TODO 系統指定
        MotifCardRecipes() + Blocks.CAKE + Items.CAKE + BlockTags.CANDLE_CAKES,
    ),
    MAGENTA_GLAZED_TERRACOTTA(
        "magenta_glazed_terracotta", 3, "Magente Glazede Terracottia", "赤紫釉陶精マゲンテグラゼデテッラツォッチャ",
        0xFFFFFF, 0xF4B5CB, 0xCB58C2, 0x9D2D95,
        PassiveSkillBuilder() + health(0.8) + luck(0.4),
        MotifCardRecipes() + Blocks.MAGENTA_GLAZED_TERRACOTTA,
    ),
    CHEST(
        "chest", 2, "Chestia", "箱精ケスチャ", 0xD6982D, 0xB3822E, 0xB3822E, 0x42392C,
        PassiveSkillBuilder()
            + collection(1.5) * indoor,
        MotifCardRecipes() + Blocks.CHEST,
    ),
    HOPPER(
        "hopper", 4, "Hopperia", "漏斗精ホッペーリャ", 0xFFFFFF, 0x797979, 0x646464, 0x5A5A5A,
        PassiveSkillBuilder()
            + collection(0.6)
            + collection(0.6) * indoor,
        MotifCardRecipes() + Blocks.HOPPER,
    ),
    BEACON(
        "beacon", 11, "Beaconia", "信標精ベアツォーニャ", 0x97FFE3, 0x6029B3, 0x2E095E, 0xD4EAE6,
        PassiveSkillBuilder()
            + StatusEffects.SPEED() * skyVisible
            + StatusEffects.RESISTANCE() * skyVisible
            + StatusEffects.JUMP_BOOST() * skyVisible
            + StatusEffects.STRENGTH() * skyVisible
            + StatusEffects.HASTE() * skyVisible
            + regeneration(0.1) * skyVisible * fairyLevel.atLeast(12.0)
            + StatusEffects.SPEED(2) * skyVisible * fairyLevel.atLeast(13.0)
            + StatusEffects.RESISTANCE(2) * skyVisible * fairyLevel.atLeast(14.0)
            + StatusEffects.JUMP_BOOST(2) * skyVisible * fairyLevel.atLeast(15.0)
            + StatusEffects.STRENGTH(2) * skyVisible * fairyLevel.atLeast(16.0)
            + StatusEffects.HASTE(2) * skyVisible * fairyLevel.atLeast(17.0),
        MotifCardRecipes() + Blocks.BEACON,
    ),
    TIME(
        "time", 14, "Timia", "時精ティーミャ", 0xCDFFBF, 0xD5DEBC, 0xD8DEA7, 0x8DD586,
        PassiveSkillBuilder()
            + StatusEffects.SPEED(2)
            + speed(0.5) * fairyLevel.atLeast(16.0),
        MotifCardRecipes().always,
    ),
    MAGNETISM(
        "magnetism", 10, "Magnetismia", "磁気精マグネティスミャ", 0xA6A6A6, 0xB33636, 0x3636B3, 0x333333,
        PassiveSkillBuilder() + collection(1.0),
        MotifCardRecipes().always,
    ),
    GRAVITY(
        "gravity", 12, "Gravitia", "重力精グラヴィーチャ", 0xC2A7F2, 0x3600FF, 0x2A00B1, 0x110047,
        PassiveSkillBuilder()
            + StatusEffects.SLOW_FALLING()
            + attack(0.5) * fairyLevel.atLeast(16.0), // TODO 射撃攻撃力
        MotifCardRecipes() + Items.APPLE,
    ),
    ANTI_ENTROPY(
        "anti_entropy", 13, "Ante Entropia", "秩序精アンテエントローピャ", 0xD4FCFF, 0x9EECFF, 0x9EECFF, 0x54C9FF,
        PassiveSkillBuilder()
            + StatusEffects.LUCK(2)
            + luck(0.5) * fairyLevel.atLeast(16.0),
        MotifCardRecipes().always,
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val translation = Translation({ "miragefairy2024.motif.${identifier.toTranslationKey()}" }, enName, jaName)
    override val displayName = translation()
    override val passiveSkillSpecifications = passiveSkillBuilder.specifications
}


// レシピ

class MotifCardRecipes {
    val recipes = mutableListOf<(MotifCard) -> Unit>()
    fun onInit(recipe: (MotifCard) -> Unit): MotifCardRecipes {
        recipes += recipe
        return this
    }
}

private fun MotifCardRecipes.common(biome: TagKey<Biome>? = null) = this.onInit { COMMON_MOTIF_RECIPES += CommonMotifRecipe(it, biome) }
private val MotifCardRecipes.always get() = this.common()
private val MotifCardRecipes.overworld get() = this.common(ConventionalBiomeTags.IN_OVERWORLD)
private val MotifCardRecipes.nether get() = this.common(ConventionalBiomeTags.IN_NETHER)
private val MotifCardRecipes.end get() = this.common(ConventionalBiomeTags.IN_THE_END)

private operator fun MotifCardRecipes.plus(item: Item) = this.onInit { FairyDreamRecipes.ITEM.register(item, it) }
private operator fun MotifCardRecipes.plus(block: Block) = this.onInit { FairyDreamRecipes.BLOCK.register(block, it) }
private operator fun MotifCardRecipes.plus(entityType: EntityType<*>) = this.onInit { FairyDreamRecipes.ENTITY_TYPE.register(entityType, it) }

@JvmName("plusItemTag")
private operator fun MotifCardRecipes.plus(tag: TagKey<Item>) = this.onInit { FairyDreamRecipes.ITEM.registerFromTag(tag, it) }

@JvmName("plusBlockTag")
private operator fun MotifCardRecipes.plus(tag: TagKey<Block>) = this.onInit { FairyDreamRecipes.BLOCK.registerFromTag(tag, it) }

@JvmName("plusEntityTypeTag")
private operator fun MotifCardRecipes.plus(tag: TagKey<EntityType<*>>) = this.onInit { FairyDreamRecipes.ENTITY_TYPE.registerFromTag(tag, it) }


// パッシブスキル

private class PassiveSkillBuilder {
    val specifications = mutableListOf<PassiveSkillSpecification<*>>()
}

private operator fun <T> PassiveSkillEffect<T>.invoke(valueProvider: (mana: Double) -> T) = PassiveSkillSpecification(listOf(), this, valueProvider)
private operator fun <T> PassiveSkillSpecification<T>.times(condition: PassiveSkillCondition) = PassiveSkillSpecification(this.conditions + condition, this.effect, this.valueProvider)
private operator fun PassiveSkillBuilder.plus(specification: PassiveSkillSpecification<*>) = this.also { it.specifications += specification }

private val overworld get() = SimplePassiveSkillConditionCard.OVERWORLD
private val outdoor get() = SimplePassiveSkillConditionCard.OUTDOOR
private val indoor get() = SimplePassiveSkillConditionCard.INDOOR
private val skyVisible get() = SimplePassiveSkillConditionCard.SKY_VISIBLE
private val fine get() = SimplePassiveSkillConditionCard.FINE
private val raining get() = SimplePassiveSkillConditionCard.RAINING
private val thundering get() = SimplePassiveSkillConditionCard.THUNDERING
private val daytime get() = SimplePassiveSkillConditionCard.DAYTIME
private val night get() = SimplePassiveSkillConditionCard.NIGHT
private val underwater get() = SimplePassiveSkillConditionCard.UNDERWATER
private val inTheAir get() = SimplePassiveSkillConditionCard.IN_THE_AIR
private val onFire get() = SimplePassiveSkillConditionCard.ON_FIRE

private fun food(item: Item) = ItemFoodIngredientPassiveSkillCondition(item)
private fun food(category: FoodIngredientCategoryCard) = CategoryFoodIngredientPassiveSkillCondition(category)

private fun IntComparisonPassiveSkillCondition.Term.atLeast(threshold: Int) = IntComparisonPassiveSkillCondition(this, true, threshold)
private fun IntComparisonPassiveSkillCondition.Term.atMost(threshold: Int) = IntComparisonPassiveSkillCondition(this, false, threshold)
private fun DoubleComparisonPassiveSkillCondition.Term.atLeast(threshold: Double) = DoubleComparisonPassiveSkillCondition(this, true, threshold)
private fun DoubleComparisonPassiveSkillCondition.Term.atMost(threshold: Double) = DoubleComparisonPassiveSkillCondition(this, false, threshold)
private val light get() = IntComparisonPassiveSkillCondition.LIGHT_LEVEL_TERM
private val food get() = IntComparisonPassiveSkillCondition.FOOD_LEVEL_TERM
private val level get() = IntComparisonPassiveSkillCondition.LEVEL_TERM
private val fairyLevel get() = DoubleComparisonPassiveSkillCondition.FAIRY_LEVEL_TERM
private val health get() = DoubleComparisonPassiveSkillCondition.HEALTH_TERM

private operator fun ToolMaterialCard.invoke() = ToolMaterialCardPassiveSkillCondition(this)

private fun mana(factor: Double) = PassiveSkillEffectCard.MANA_BOOST { it * factor * 0.02 }
private fun attribute(attribute: EntityAttribute, factor: Double) = PassiveSkillEffectCard.ENTITY_ATTRIBUTE { EntityAttributePassiveSkillEffect.Value(mapOf(attribute to it * factor)) }
private fun attack(factor: Double) = attribute(EntityAttributes.GENERIC_ATTACK_DAMAGE, factor * 0.1)
private fun speed(factor: Double) = attribute(EntityAttributes.GENERIC_MOVEMENT_SPEED, factor * 0.002)
private fun health(factor: Double) = attribute(EntityAttributes.GENERIC_MAX_HEALTH, factor * 0.4)
private fun luck(factor: Double) = attribute(EntityAttributes.GENERIC_LUCK, factor * 0.1)
private operator fun StatusEffect.invoke(level: Int = 1, additionalSeconds: Int = 0): PassiveSkillSpecification<StatusEffectPassiveSkillEffect.Value> {
    return PassiveSkillEffectCard.STATUS_EFFECT { StatusEffectPassiveSkillEffect.Value(mapOf(this@invoke to StatusEffectPassiveSkillEffect.Entry(level, additionalSeconds))) }
}

private val ignition get() = PassiveSkillEffectCard.IGNITION { true }
private fun experience(factor: Double) = PassiveSkillEffectCard.EXPERIENCE { it * factor * 0.005 }
private fun regeneration(factor: Double) = PassiveSkillEffectCard.REGENERATION { it * factor * 0.01 }
private fun mending(factor: Double) = PassiveSkillEffectCard.MENDING { it * factor * 0.01 }
private fun collection(factor: Double) = PassiveSkillEffectCard.COLLECTION { it * factor * 0.1 }


fun initMotif() {
    MotifCard.entries.forEach { card ->
        card.register(motifRegistry, card.identifier)
        card.translation.enJa()
        card.recipes.recipes.forEach {
            it(card)
        }
    }

    registerDebugItem("dump_fairy_motifs", Items.STRING, 0xF200FF) { world, player, _, _ ->
        if (!world.isClient) return@registerDebugItem
        val sb = StringBuilder()
        motifRegistry.sortedBy { if (it is MotifCard) it.ordinal else 99999999 }.forEach { motif ->
            sb.append("|${motif.displayName.string}|${motif.rare}|")
            motif.passiveSkillSpecifications.forEachIndexed { index, specification ->
                fun <T> f(specification: PassiveSkillSpecification<T>) {
                    if (index > 0) sb.append("&br;")
                    sb.append(specification.effect.getText(specification.valueProvider(motif.rare.toDouble())).string)
                }
                f(specification)
            }
            sb.append("|")
            motif.passiveSkillSpecifications.forEachIndexed { index, specification ->
                fun <T> f(specification: PassiveSkillSpecification<T>) {
                    if (index > 0) sb.append("&br;")
                    sb.append(specification.effect.getText(specification.valueProvider(10.0)).string)
                }
                f(specification)
            }
            sb.append("|")
            motif.passiveSkillSpecifications.forEachIndexed { index, specification ->
                fun <T> f(specification: PassiveSkillSpecification<T>) {
                    if (index > 0) sb.append("&br;")
                    sb.append(if (specification.conditions.isNotEmpty()) "[" + specification.conditions.map { it.text.string }.join(",") + "]" else "　")
                }
                f(specification)
            }
            sb.append("|")
            sb.append("\n")
        }
        writeAction(player, "dump_fairy_motifs.txt", Emoji.entries.fold(sb.toString()) { s, e -> s.replace(e.charCode, e.string) })
    }
}
