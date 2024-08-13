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
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import kotlin.math.pow

enum class TraitEffectKeyCard(
    path: String,
    val enName: String,
    val jaName: String,
    sortValue: Double,
    style: Style,
    isLogScale: Boolean,
) {
    NUTRITION("nutrition", "NTR", "栄養値", 1100.0, Style.EMPTY.withColor(0xCECECE), false),
    ENVIRONMENT("environment", "ENV", "環境値", 1200.0, Style.EMPTY.withColor(0xA0CC8E), false),

    SEEDS_PRODUCTION("seeds_production", "SEED", "種子生成", 2100.0, Style.EMPTY.withColor(0xFFC587), false),
    FRUITS_PRODUCTION("fruits_production", "FRUIT", "果実生成", 2200.0, Style.EMPTY.withColor(0xFF87BF), false),
    LEAVES_PRODUCTION("leaves_production", "LEAF", "葉面生成", 2300.0, Style.EMPTY.withColor(0x32C900), false),
    RARE_PRODUCTION("rare_production", "RARE", "希少品生成", 2400.0, Style.EMPTY.withColor(0x00E2E2), false),
    EXPERIENCE_PRODUCTION("experience_production", "XP", "経験値", 2500.0, Style.EMPTY.withColor(0xEFEF00), false),

    GROWTH_BOOST("growth_boost", "GRW", "成長速度", 3100.0, Style.EMPTY.withColor(0x00C600), false),
    PRODUCTION_BOOST("production_boost", "PRD", "生産能力", 3200.0, Style.EMPTY.withColor(0xFF4242), false),

    FORTUNE_FACTOR("fortune_factor", "FTN", "幸運係数", 4100.0, Style.EMPTY.withColor(0xFF4FFF), false),
    NATURAL_ABSCISSION("natural_abscission", "NA", "自然落果", 4200.0, Style.EMPTY.withColor(0x5959FF), true),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val traitEffectKey = if (isLogScale) {
        object : TraitEffectKey<Double>() {
            override val sortValue = sortValue
            override val style = style
            override fun getValue(level: Double) = 1 - 0.95.pow(level)
            override fun getDescription(value: Double) = text { getName() + (value * 100 formatAs "%+.0f%%")() }
            override fun plus(a: Double, b: Double) = 1.0 - (1.0 - a) * (1.0 - b)
            override fun getDefaultValue() = 0.0
        }
    } else {
        object : TraitEffectKey<Double>() {
            override val sortValue = sortValue
            override val style = style
            override fun getValue(level: Double) = 0.1 * level
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
