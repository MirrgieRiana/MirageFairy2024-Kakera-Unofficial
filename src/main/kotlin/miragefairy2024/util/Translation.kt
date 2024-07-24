package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.minecraft.block.Block
import net.minecraft.item.Item

context(ModContext)
fun en(getter: () -> Pair<String, String>) = ModEvents.onInitialize {
    DataGenerationEvents.englishTranslationGenerators {
        val pair = getter()
        it.add(pair.first, pair.second)
    }
}

context(ModContext)
fun ja(getter: () -> Pair<String, String>) = ModEvents.onInitialize {
    DataGenerationEvents.japaneseTranslationGenerators {
        val pair = getter()
        it.add(pair.first, pair.second)
    }
}

context(ModContext)
fun Block.enJa(enName: String, jaName: String) {
    en { this.translationKey to enName }
    ja { this.translationKey to jaName }
}

context(ModContext)
fun Item.enJa(enName: String, jaName: String) {
    en { this.translationKey to enName }
    ja { this.translationKey to jaName }
}


class Translation(val keyGetter: () -> String, val en: String, val ja: String)

operator fun Translation.invoke() = text { translate(this@invoke.keyGetter()) }

operator fun Translation.invoke(vararg args: Any?) = text { translate(this@invoke.keyGetter(), *args) }

context(ModContext)
fun Translation.enJa() {
    en { this.keyGetter() to en }
    ja { this.keyGetter() to ja }
}
