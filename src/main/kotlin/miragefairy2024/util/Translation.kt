package miragefairy2024.util

import miragefairy2024.InitializationContext
import miragefairy2024.MirageFairy2024DataGenerator
import net.minecraft.block.Block
import net.minecraft.item.Item

context(InitializationContext)
fun en(getter: () -> Pair<String, String>) = MirageFairy2024DataGenerator.englishTranslationGenerators {
    val pair = getter()
    it.add(pair.first, pair.second)
}

context(InitializationContext)
fun ja(getter: () -> Pair<String, String>) = MirageFairy2024DataGenerator.japaneseTranslationGenerators {
    val pair = getter()
    it.add(pair.first, pair.second)
}

context(InitializationContext)
fun Block.enJa(enName: String, jaName: String) {
    en { this.translationKey to enName }
    ja { this.translationKey to jaName }
}

context(InitializationContext)
fun Item.enJa(enName: String, jaName: String) {
    en { this.translationKey to enName }
    ja { this.translationKey to jaName }
}


class Translation(val keyGetter: () -> String, val en: String, val ja: String)

operator fun Translation.invoke() = text { translate(this@invoke.keyGetter()) }

operator fun Translation.invoke(vararg args: Any?) = text { translate(this@invoke.keyGetter(), *args) }

context(InitializationContext)
fun Translation.enJa() {
    en { this.keyGetter() to en }
    ja { this.keyGetter() to ja }
}
