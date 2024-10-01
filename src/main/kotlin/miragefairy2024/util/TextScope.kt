package miragefairy2024.util

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.io.File

inline fun text(block: TextScope.() -> Text) = block(TextScope())

open class TextScope {
    fun empty(): Text = Text.empty()
    operator fun String.invoke(): Text = Text.of(this)
    fun translate(key: String): Text = Text.translatable(key)
    fun translate(key: String, vararg args: Any?): Text = Text.translatable(key, *args)
    operator fun Text.plus(text: Text): Text = Text.empty().append(this).append(text)
    operator fun File.invoke() = Text.literal(name).styled {
        it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text { absoluteFile.canonicalPath() }))
        it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_FILE, absoluteFile.canonicalPath))
    }.underline
}
