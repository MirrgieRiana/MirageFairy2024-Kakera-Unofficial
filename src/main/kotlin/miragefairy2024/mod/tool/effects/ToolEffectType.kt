package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolEffectType
import mirrg.kotlin.hydrogen.max

abstract class BooleanToolEffectType : ToolEffectType<Boolean> {
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun merge(a: Boolean, b: Boolean) = a || b
}

abstract class IntAddToolEffectType : ToolEffectType<Int> {
    override fun castOrThrow(value: Any?) = value as Int
    override fun merge(a: Int, b: Int) = a + b
}

abstract class IntMaxToolEffectType : ToolEffectType<Int> {
    override fun castOrThrow(value: Any?) = value as Int
    override fun merge(a: Int, b: Int) = a max b
}
