package miragefairy2024.util

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.io.File

inline fun text(block: TextScope.() -> Text) = block(TextScope)


object TextScope

context(TextScope)
fun empty(): Text = Text.empty()

context(TextScope)
operator fun String.invoke(): Text = Text.of(this)

context(TextScope)
fun translate(key: String): Text = Text.translatable(key)

context(TextScope)
fun translate(key: String, vararg args: Any?): Text = Text.translatable(key, *args)

context(TextScope)
operator fun Text.plus(text: Text): Text = Text.empty().append(this).append(text)

context(TextScope)
operator fun File.invoke() = Text.literal(name).styled {
    it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text { absoluteFile.canonicalPath() }))
    it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_FILE, absoluteFile.canonicalPath))
}.underline
