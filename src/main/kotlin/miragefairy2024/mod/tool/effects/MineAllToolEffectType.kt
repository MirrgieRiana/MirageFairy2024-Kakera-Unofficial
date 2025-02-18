package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.items.mineAll

object MineAllToolEffectType : ToolEffectType<MineAllToolEffectType.Value> {
    class Value(val configuration: ToolConfiguration, val enabled: Boolean)

    override fun castOrThrow(value: Any) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.configuration, a.enabled || b.enabled)
    override fun apply(value: Value) {
        value.configuration.onPostMineListeners += { item, stack, world, state, pos, miner ->
            item.mineAll(stack, world, state, pos, miner, value.configuration)
        }
    }
}
