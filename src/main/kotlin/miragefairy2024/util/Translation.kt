package miragefairy2024.util

import miragefairy2024.MirageFairy2024DataGenerator
import net.minecraft.block.Block
import net.minecraft.item.Item

fun en(getter: () -> Pair<String, String>) = MirageFairy2024DataGenerator.englishTranslationGenerators {
    val pair = getter()
    it.add(pair.first, pair.second)
}

fun ja(getter: () -> Pair<String, String>) = MirageFairy2024DataGenerator.japaneseTranslationGenerators {
    val pair = getter()
    it.add(pair.first, pair.second)
}

fun Block.enJa(enName: String, jaName: String) {
    en { this.translationKey to enName }
    ja { this.translationKey to jaName }
}

fun Item.enJa(enName: String, jaName: String) {
    en { this.translationKey to enName }
    ja { this.translationKey to jaName }
}


class Translation(val keyGetter: () -> String, val en: String, val ja: String)

operator fun Translation.invoke() = text { translate(this@invoke.keyGetter()) }

operator fun Translation.invoke(vararg args: Any?) = text { translate(this@invoke.keyGetter(), *args) }

fun Translation.enJa() {
    en { this.keyGetter() to en }
    ja { this.keyGetter() to ja }
}
