package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item

context(ModContext)
fun en(getter: () -> Pair<String, String>) = DataGenerationEvents.onGenerateEnglishTranslation {
    val pair = getter()
    it.add(pair.first, pair.second)
}

context(ModContext)
fun ja(getter: () -> Pair<String, String>) = DataGenerationEvents.onGenerateJapaneseTranslation {
    val pair = getter()
    it.add(pair.first, pair.second)
}

context(ModContext)
fun Block.enJa(name: EnJa) {
    en { this.translationKey to name.en }
    ja { this.translationKey to name.ja }
}

context(ModContext)
fun Item.enJa(name: EnJa) {
    en { this.translationKey to name.en }
    ja { this.translationKey to name.ja }
}

context(ModContext)
fun EntityType<*>.enJa(name: EnJa) {
    en { this.translationKey to name.en }
    ja { this.translationKey to name.ja }
}


class EnJa(val en: String, val ja: String)


class Translation(val keyGetter: () -> String, val en: String, val ja: String)

fun Translation(keyGetter: () -> String, enJa: EnJa) = Translation(keyGetter, enJa.en, enJa.ja)

context(TextScope)
operator fun Translation.invoke() = text { translate(this@invoke.keyGetter()) }

context(TextScope)
operator fun Translation.invoke(vararg args: Any?) = text { translate(this@invoke.keyGetter(), *args) }

context(ModContext)
fun Translation.enJa() {
    en { this.keyGetter() to en }
    ja { this.keyGetter() to ja }
}
