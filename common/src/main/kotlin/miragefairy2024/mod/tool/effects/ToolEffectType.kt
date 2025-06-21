package miragefairy2024.mod.tool.effects

import miragefairy2024.ModContext
import miragefairy2024.mod.tool.ToolEffectType
import mirrg.kotlin.hydrogen.max

context(ModContext)
fun initToolEffectType() {
    AreaMiningToolEffectType.init()
    MineAllToolEffectType.init()
    CutAllToolEffectType.init()
    SelfMendingToolEffectType.init()
    ObtainFairyToolEffectType.init()
    CollectionToolEffectType.init()
    SoulStreamContainableToolEffectType.init()
    RoughenHoeToolEffectType.init()
}

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

abstract class DoubleAddToolEffectType : ToolEffectType<Double> {
    override fun castOrThrow(value: Any?) = value as Double
    override fun merge(a: Double, b: Double) = a + b
}
