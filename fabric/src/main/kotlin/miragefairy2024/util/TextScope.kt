package miragefairy2024.util

import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import java.io.File

inline fun text(block: TextScope.() -> Component) = block(TextScope)


object TextScope

context(TextScope)
fun empty(): Component = Component.empty()

context(TextScope)
operator fun String.invoke(): Component = Component.nullToEmpty(this)

context(TextScope)
fun translate(key: String): Component = Component.translatable(key)

context(TextScope)
fun translate(key: String, vararg args: Any?): Component = Component.translatable(key, *args)

context(TextScope)
operator fun Component.plus(text: Component): Component = Component.empty().append(this).append(text)

context(TextScope)
operator fun File.invoke() = Component.literal(name).withStyle {
    it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text { absoluteFile.canonicalPath() }))
    it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_FILE, absoluteFile.canonicalPath))
}.underline
