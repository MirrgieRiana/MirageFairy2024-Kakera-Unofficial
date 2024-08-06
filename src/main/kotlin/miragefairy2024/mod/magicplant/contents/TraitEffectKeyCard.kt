package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.TraitEffectKey
import miragefairy2024.mod.magicplant.enJa
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.traitEffectKeyRegistry
import miragefairy2024.util.register
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import kotlin.math.pow

enum class TraitEffectKeyCard(
    path: String,
    val enName: String,
    val jaName: String,
    val color: Formatting,
    isLogScale: Boolean,
) {
    NUTRITION("nutrition", "NTR", "栄養値", Formatting.AQUA, false),
    ENVIRONMENT("environment", "ENV", "環境値", Formatting.GREEN, false),
    GROWTH_BOOST("growth_boost", "GRW", "成長速度", Formatting.DARK_BLUE, false),
    SEEDS_PRODUCTION("seeds_production", "SEED", "種子生成", Formatting.RED, false),
    FRUITS_PRODUCTION("fruits_production", "FRUIT", "果実生成", Formatting.LIGHT_PURPLE, false),
    LEAVES_PRODUCTION("leaves_production", "LEAF", "葉面生成", Formatting.DARK_GREEN, false),
    RARE_PRODUCTION("rare_production", "RARE", "希少品生成", Formatting.GOLD, false),
    PRODUCTION_BOOST("production_boost", "PRD", "生産能力", Formatting.DARK_RED, false),
    EXPERIENCE_PRODUCTION("experience_production", "XP", "経験値", Formatting.YELLOW, false),
    FORTUNE_FACTOR("fortune_factor", "FTN", "幸運係数", Formatting.DARK_PURPLE, false),
    NATURAL_ABSCISSION("natural_abscission", "NA", "自然落果", Formatting.BLUE, true),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val traitEffectKey = if (isLogScale) {
        object : TraitEffectKey<Double>() {
            override fun getValue(level: Int) = 1 - 0.95.pow(level.toDouble())
            override fun getDescription(value: Double) = text { getName() + (value * 100 formatAs "%+.0f%%")() }
            override fun plus(a: Double, b: Double) = 1.0 - (1.0 - a) * (1.0 - b)
            override fun getDefaultValue() = 0.0
        }
    } else {
        object : TraitEffectKey<Double>() {
            override fun getValue(level: Int) = 0.1 * level
            override fun getDescription(value: Double) = text { getName() + (value * 100 formatAs "%+.0f%%")() }
            override fun plus(a: Double, b: Double) = a + b
            override fun getDefaultValue() = 0.0
        }
    }
}

context(ModContext)
fun initTraitEffectKeyCard() {
    TraitEffectKeyCard.entries.forEach { card ->
        card.traitEffectKey.register(traitEffectKeyRegistry, card.identifier)
        card.traitEffectKey.enJa(card.enName, card.jaName)
    }
}
