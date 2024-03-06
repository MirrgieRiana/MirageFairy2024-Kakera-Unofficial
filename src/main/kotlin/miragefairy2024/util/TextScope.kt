package miragefairy2024.util

import net.minecraft.text.Text

inline fun text(block: TextScope.() -> Text) = block(TextScope())

open class TextScope {
    fun empty(): Text = Text.empty()
    operator fun String.invoke(): Text = Text.of(this)
    fun translate(key: String): Text = Text.translatable(key)
    fun translate(key: String, vararg args: Any?): Text = Text.translatable(key, *args)
    operator fun Text.plus(text: Text): Text = Text.empty().append(this).append(text)
}

fun buildText(block: BuildTextScope.() -> Unit) = BuildTextScope().also { block(it) }.build()

class BuildTextScope : TextScope() {
    private val texts = mutableListOf<Text>()

    operator fun Text.not() {
        texts += this
    }

    fun build() = texts.join()
}
