package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.FoodIngredientCategory
import miragefairy2024.mod.containsAsFoodIngredient
import miragefairy2024.mod.invoke
import miragefairy2024.mod.lastFood
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import miragefairy2024.util.orEmpty
import miragefairy2024.util.removeTrailingZeros
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.FluidTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.Biome
import java.time.Instant

fun initPassiveSkillConditions() = ModEvents.onInitialize {
    SimplePassiveSkillConditionCard.entries.forEach { card ->
        card.init()
    }
    ItemFoodIngredientPassiveSkillCondition.translation.enJa()
    MainHandConditionCard.entries.forEach { card ->
        card.translation.enJa()
    }
}


// simple

private fun isOutdoor(context: PassiveSkillContext) = context.blockPos.y >= context.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, context.blockPos).y
private fun biomeHasNoPrecipitation(context: PassiveSkillContext) = context.world.getBiome(context.blockPos).value().getPrecipitation(context.blockPos) == Biome.Precipitation.NONE
private fun biomeHasRain(context: PassiveSkillContext) = context.world.getBiome(context.blockPos).value().getPrecipitation(context.blockPos) == Biome.Precipitation.RAIN
private fun isDaytime(context: PassiveSkillContext): Boolean {
    context.world.calculateAmbientDarkness()
    return context.world.ambientDarkness < 4
}

enum class SimplePassiveSkillConditionCard(path: String, enName: String, jaName: String, private val function: (context: PassiveSkillContext) -> Boolean) : PassiveSkillCondition {
    OVERWORLD("overworld", "Overworld", "地上世界", { it.world.dimension.natural }),
    OUTDOOR("outdoor", "Outdoor", "屋外", { isOutdoor(it) }),
    INDOOR("indoor", "Indoor", "屋内", { !isOutdoor(it) }),
    SKY_VISIBLE("sky_visible", "Sky Visible", "空が見える", { it.world.isSkyVisible(it.blockPos) }),
    FINE("fine", "Fine", "晴天", { !it.world.isRaining || biomeHasNoPrecipitation(it) }),
    RAINING("raining", "Raining", "雨天", { it.world.isRaining && biomeHasRain(it) }),
    THUNDERING("thundering", "Thundering", "雷雨", { it.world.isThundering && !biomeHasNoPrecipitation(it) }),
    DAYTIME("daytime", "Daytime", "昼間", { isDaytime(it) }),
    NIGHT("night", "Night", "夜間", { !isDaytime(it) }),
    UNDERWATER("underwater", "Underwater", "水中", { it.player.world.getBlockState(it.player.eyeBlockPos).fluidState.isIn(FluidTags.WATER) }),
    IN_THE_AIR("in_the_air", "In the Air", "空中", { !it.player.isOnGround }),
    ON_FIRE("on_fire", "On Fire", "炎上", { it.player.isOnFire }),
    IN_NETHER("in_nether", "In Nether", "ネザー", { it.world.getBiome(it.blockPos).isIn(ConventionalBiomeTags.IN_NETHER) }),
    NOT_IN_NETHER("not_in_nether", "Not In Nether", "ネザー外", { !it.world.getBiome(it.blockPos).isIn(ConventionalBiomeTags.IN_NETHER) }),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val translation = Translation({ "miragefairy2024.passive_skill_condition.${identifier.toTranslationKey()}" }, enName, jaName)

    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = function(context)
    override val text = translation()

    fun init() {
        translation.enJa()
    }
}


// comparison

class IntComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Int) : PassiveSkillCondition {
    companion object {
        val LIGHT_LEVEL_TERM = Term(Emoji.LIGHT) { context, _, _ -> context.player.world.getLightLevel(context.player.eyeBlockPos) }
        val FOOD_LEVEL_TERM = Term(Emoji.FOOD, 2) { context, _, _ -> context.player.hungerManager.foodLevel }
        val LEVEL_TERM = Term(Emoji.LEVEL) { context, _, _ -> context.player.experienceLevel }
    }

    class Term(val emoji: Emoji, val unit: Int = 1, val getValue: (context: PassiveSkillContext, level: Double, mana: Double) -> Int)

    private fun format(double: Double) = (double formatAs "%.8f").removeTrailingZeros()
    override val text: Text get() = text { term.emoji() + format(threshold / term.unit.toDouble())() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        val value = term.getValue(context, level, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}

class DoubleComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Double) : PassiveSkillCondition {
    companion object {
        val FAIRY_LEVEL_TERM = Term(Emoji.STAR) { _, level, _ -> level }
        val MANA_TERM = Term(Emoji.MANA) { _, _, mana -> mana }
        val HEALTH_TERM = Term(Emoji.HEART, 2.0) { context, _, _ -> context.player.health.toDouble() }
    }

    class Term(val emoji: Emoji, val unit: Double = 1.0, val getValue: (context: PassiveSkillContext, level: Double, mana: Double) -> Double)

    private fun format(double: Double) = (double formatAs "%.8f").removeTrailingZeros()
    override val text: Text get() = text { term.emoji() + format(threshold / term.unit)() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        val value = term.getValue(context, level, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}


// food ingredient

class ItemFoodIngredientPassiveSkillCondition(private val item: Item) : PassiveSkillCondition {
    companion object {
        val identifier = Identifier(MirageFairy2024.modId, "food_ingredient")
        val translation = Translation({ "miragefairy2024.passive_skill_condition.${identifier.toTranslationKey()}" }, "%s Dishes", "%s料理")
    }

    override val text get() = translation(item.name)
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        if (!(context.player.lastFood.itemStack.orEmpty.item containsAsFoodIngredient item)) return false
        val time = context.player.lastFood.time ?: return false
        val now = Instant.now()
        return time in now.minusSeconds(3600 * 25)..now
    }
}

class CategoryFoodIngredientPassiveSkillCondition(private val category: FoodIngredientCategory) : PassiveSkillCondition {
    override val text get() = ItemFoodIngredientPassiveSkillCondition.translation(category.text)
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        if (!(context.player.lastFood.itemStack.orEmpty.item containsAsFoodIngredient category)) return false
        val time = context.player.lastFood.time ?: return false
        val now = Instant.now()
        return time in now.minusSeconds(3600 * 25)..now
    }
}


// tool material card

class ToolMaterialCardPassiveSkillCondition(private val card: ToolMaterialCard) : PassiveSkillCondition {
    override val text: Text get() = card.translation()
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.mainHandStack.isIn(card.tag)
}


// main hand

enum class MainHandConditionCard(path: String, en: String, ja: String, val predicate: (ItemStack) -> Boolean) {
    HOE("hoe", "Hoe", "クワ", { it.isIn(ItemTags.HOES) }),
    SWORD("sword", "Sword", "剣", { it.isIn(ItemTags.SWORDS) }),
    ;

    val translation = Translation({ "${MirageFairy2024.modId}.passive_skill_condition.main_hand.$path" }, en, ja)
}

class MainHandPassiveSkillCondition(private val card: MainHandConditionCard) : PassiveSkillCondition {
    override val text: Text get() = card.translation()
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = card.predicate(context.player.mainHandStack)
}


// status effect

class StatusEffectPassiveSkillCondition(private val statusEffect: StatusEffect) : PassiveSkillCondition {
    override val text: Text get() = statusEffect.name
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.hasStatusEffect(statusEffect)
}
