package miragefairy2024.util

import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component as Text
import net.minecraft.ChatFormatting as Formatting

fun Text.style(style: Style): Text = Text.empty().append(this).setStyle(style)
fun Text.formatted(formatting: Formatting): Text = Text.empty().append(this).formatted(formatting)
val Text.black get() = this.formatted(Formatting.BLACK)
val Text.darkBlue get() = this.formatted(Formatting.DARK_BLUE)
val Text.darkGreen get() = this.formatted(Formatting.DARK_GREEN)
val Text.darkAqua get() = this.formatted(Formatting.DARK_AQUA)
val Text.darkRed get() = this.formatted(Formatting.DARK_RED)
val Text.darkPurple get() = this.formatted(Formatting.DARK_PURPLE)
val Text.gold get() = this.formatted(Formatting.GOLD)
val Text.gray get() = this.formatted(Formatting.GRAY)
val Text.darkGray get() = this.formatted(Formatting.DARK_GRAY)
val Text.blue get() = this.formatted(Formatting.BLUE)
val Text.green get() = this.formatted(Formatting.GREEN)
val Text.aqua get() = this.formatted(Formatting.AQUA)
val Text.red get() = this.formatted(Formatting.RED)
val Text.lightPurple get() = this.formatted(Formatting.LIGHT_PURPLE)
val Text.yellow get() = this.formatted(Formatting.YELLOW)
val Text.white get() = this.formatted(Formatting.WHITE)
val Text.obfuscated get() = this.formatted(Formatting.OBFUSCATED)
val Text.bold get() = this.formatted(Formatting.BOLD)
val Text.strikethrough get() = this.formatted(Formatting.STRIKETHROUGH)
val Text.underline get() = this.formatted(Formatting.UNDERLINE)
val Text.italic get() = this.formatted(Formatting.ITALIC)

fun Iterable<Text>.join(): Text {
    val result = Text.empty()
    this.forEach {
        result.append(it)
    }
    return result
}

fun Iterable<Text>.join(vararg separators: Text): Text {
    val result = Text.empty()
    this.forEachIndexed { index, text ->
        if (index != 0) {
            separators.forEach {
                result.append(it)
            }
        }
        result.append(text)
    }
    return result
}

fun Int.toRomanText() = if (this in 1..10) text { translate("enchantment.level.${this@toRomanText}") } else text { "$this"() }
