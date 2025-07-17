package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.mod.magicplant.TraitEffectKey
import miragefairy2024.mod.magicplant.traitEffectKeyRegistry
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.string
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.network.chat.Component
import kotlin.math.pow

enum class TraitEffectKeyCard(
    path: String,
    emoji: Emoji,
    enName: String,
    jaName: String,
    sortValue: Double,
    color: Int,
    isLogScale: Boolean,
) {
    NUTRITION("nutrition", Emoji.NUTRITION, "Nutrition", "栄養値", 1100.0, 0x99DD99, false),
    TEMPERATURE("temperature", Emoji.MEDIUM_TEMPERATURE, "Temperature Environment", "気温環境値", 1200.0, 0xE89D84, false),
    HUMIDITY("humidity", Emoji.MEDIUM_HUMIDITY, "Humidity Environment", "湿度環境値", 1300.0, 0x8ECCCC, false),

    SEEDS_PRODUCTION("seeds_production", Emoji.SEEDS_PRODUCTION, "Seeds Production", "種子生成", 2100.0, 0xFFC587, false),
    FRUITS_PRODUCTION("fruits_production", Emoji.FRUITS_PRODUCTION, "Fruits Production", "果実生成", 2200.0, 0xFF87BF, false),
    LEAVES_PRODUCTION("leaves_production", Emoji.LEAVES_PRODUCTION, "Leaves Production", "葉面生成", 2300.0, 0x32C900, false),
    RARE_PRODUCTION("rare_production", Emoji.RARE_PRODUCTION, "Rare Production", "希少品生成", 2400.0, 0x00E2E2, false),
    SPECIAL_PRODUCTION("special_production", Emoji.RARE_PRODUCTION, "Special Production", "特殊品生成", 2500.0, 0x00D390, false),
    EXPERIENCE_PRODUCTION("experience_production", Emoji.LEVEL, "Experience Production", "経験値生成", 2600.0, 0xEFEF00, false),

    GROWTH_BOOST("growth_boost", Emoji.GROWTH_BOOST, "Growth Boost", "成長速度ブースト", 3100.0, 0x00C600, false),
    PRODUCTION_BOOST("production_boost", Emoji.PRODUCTION_BOOST, "Production Boost", "生産量ブースト", 3200.0, 0xFF4242, false),

    FORTUNE_FACTOR("fortune_factor", Emoji.MANA, "Fortune Factor", "幸運係数", 4100.0, 0xFF4FFF, false),
    NATURAL_ABSCISSION("natural_abscission", Emoji.NATURAL_ABSCISSION, "Natural Abscission", "自然落果", 4200.0, 0x5959FF, true),
    CROSSBREEDING("crossbreeding", Emoji.CROSSBREEDING, "Crossbreeding", "交雑", 4300.0, 0xFFA011, true),
    MUTATION("mutation", Emoji.MUTATION, "Mutation", "突然変異", 4400.0, 0xFF668C, true),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val translation = Translation({ identifier.toLanguageKey("${MirageFairy2024.MOD_ID}.trait_effect") }, enName, jaName)
    val traitEffectKey = if (isLogScale) {
        object : TraitEffectKey<Double>() {
            override val emoji = emoji()
            override val name = text { translation() }
            override val sortValue = sortValue
            override val color = color
            override fun getValue(level: Double) = 1 - 0.5.pow(level)
            override fun renderValue(value: Double): Component {
                return when {
                    value < 0.1 -> text { (value * 100.0 formatAs "%.1f%%")() }
                    else -> text { (value * 100.0 formatAs "%.0f%%")() }
                }
            }

            override fun plus(a: Double, b: Double) = 1.0 - (1.0 - a) * (1.0 - b)
            override fun getDefaultValue() = 0.0
            override fun toString() = identifier.string
        }
    } else {
        object : TraitEffectKey<Double>() {
            override val emoji = emoji()
            override val name = text { translation() }
            override val sortValue = sortValue
            override val color = color
            override fun getValue(level: Double) = level
            override fun renderValue(value: Double): Component {
                return when {
                    value < 0.1 -> text { (value * 100.0 formatAs "%.1f%%")() }
                    else -> text { (value * 100.0 formatAs "%.0f%%")() }
                }
            }

            override fun plus(a: Double, b: Double) = a + b
            override fun getDefaultValue() = 0.0
            override fun toString() = identifier.string
        }
    }
}

context(ModContext)
fun initTraitEffectKeyCard() {
    TraitEffectKeyCard.entries.forEach { card ->
        Registration(traitEffectKeyRegistry, card.identifier) { card.traitEffectKey }.register()
        card.translation.enJa()
    }
}
