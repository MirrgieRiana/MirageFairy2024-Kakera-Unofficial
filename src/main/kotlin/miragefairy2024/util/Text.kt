package miragefairy2024.util

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.ChatFormatting as Formatting

fun Component.style(style: Style): Component = Component.empty().append(this).setStyle(style)
fun Component.formatted(formatting: Formatting): Component = Component.empty().append(this).withStyle(formatting)
val Component.black get() = this.formatted(Formatting.BLACK)
val Component.darkBlue get() = this.formatted(Formatting.DARK_BLUE)
val Component.darkGreen get() = this.formatted(Formatting.DARK_GREEN)
val Component.darkAqua get() = this.formatted(Formatting.DARK_AQUA)
val Component.darkRed get() = this.formatted(Formatting.DARK_RED)
val Component.darkPurple get() = this.formatted(Formatting.DARK_PURPLE)
val Component.gold get() = this.formatted(Formatting.GOLD)
val Component.gray get() = this.formatted(Formatting.GRAY)
val Component.darkGray get() = this.formatted(Formatting.DARK_GRAY)
val Component.blue get() = this.formatted(Formatting.BLUE)
val Component.green get() = this.formatted(Formatting.GREEN)
val Component.aqua get() = this.formatted(Formatting.AQUA)
val Component.red get() = this.formatted(Formatting.RED)
val Component.lightPurple get() = this.formatted(Formatting.LIGHT_PURPLE)
val Component.yellow get() = this.formatted(Formatting.YELLOW)
val Component.white get() = this.formatted(Formatting.WHITE)
val Component.obfuscated get() = this.formatted(Formatting.OBFUSCATED)
val Component.bold get() = this.formatted(Formatting.BOLD)
val Component.strikethrough get() = this.formatted(Formatting.STRIKETHROUGH)
val Component.underline get() = this.formatted(Formatting.UNDERLINE)
val Component.italic get() = this.formatted(Formatting.ITALIC)

fun Iterable<Component>.join(): Component {
    val result = Component.empty()
    this.forEach {
        result.append(it)
    }
    return result
}

fun Iterable<Component>.join(vararg separators: Component): Component {
    val result = Component.empty()
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
