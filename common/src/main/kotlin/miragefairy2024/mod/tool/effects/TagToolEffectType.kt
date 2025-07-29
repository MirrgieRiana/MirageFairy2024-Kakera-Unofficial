package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.merge
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

fun <T : ToolConfiguration> T.tag(vararg tags: TagKey<Item>) = this.merge(TagToolEffectType, TagToolEffectType.Value(tags.toSet()))

object TagToolEffectType : ToolEffectType<ToolConfiguration, TagToolEffectType.Value> {
    class Value(val tags: Set<TagKey<Item>>)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.tags + b.tags)

    override fun apply(configuration: ToolConfiguration, value: Value) {
        if (value.tags.isEmpty()) return
        configuration.tags += value.tags - configuration.tags
    }
}
