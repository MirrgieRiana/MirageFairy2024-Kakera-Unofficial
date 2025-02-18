package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolEffectType

abstract class BooleanToolEffectType : ToolEffectType<Boolean> {
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun merge(a: Boolean, b: Boolean) = a || b
}
