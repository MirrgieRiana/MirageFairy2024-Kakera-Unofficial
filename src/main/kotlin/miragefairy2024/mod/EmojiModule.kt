package miragefairy2024.mod

import miragefairy2024.util.text

enum class Emoji(val string: String, val charCode: String) {
    STAR("★", "\uE600"),
    HEART("❤", "\uE601"),
    FOOD("🍖", "\uE602"),
    LEVEL("Lv", "\uE603"),
    LUCK("🍀", "\uE604"),
    LIGHT("💡", "\uE605"),
    UP("↑", "\uE606"),
    DOWN("↓", "\uE607"),
    MANA("◇", "\uE608"),
    ;

    override fun toString() = charCode
}

operator fun Emoji.invoke() = text { this@invoke.charCode() }
