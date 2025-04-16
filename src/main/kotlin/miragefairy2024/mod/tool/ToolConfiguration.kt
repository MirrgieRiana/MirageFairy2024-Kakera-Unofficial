package miragefairy2024.mod.tool

import miragefairy2024.ModContext
import miragefairy2024.mixin.api.BlockCallback
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.text
import miragefairy2024.mod.tool.effects.AreaMiningToolEffectType
import miragefairy2024.mod.tool.effects.CollectionToolEffectType
import miragefairy2024.mod.tool.effects.CutAllToolEffectType
import miragefairy2024.mod.tool.effects.MineAllToolEffectType
import miragefairy2024.mod.tool.effects.ObtainFairyToolEffectType
import miragefairy2024.mod.tool.effects.SelfMendingToolEffectType
import miragefairy2024.mod.tool.effects.SoulStreamContainableToolEffectType
import miragefairy2024.mod.tool.items.FairyToolItem
import miragefairy2024.mod.tool.items.onAfterBreakBlock
import miragefairy2024.mod.tool.items.onKilled
import miragefairy2024.util.registerItemTagGeneration
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.tags.TagKey
import net.minecraft.network.chat.Component as Text
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World

context(ModContext)
fun initToolConfiguration() {

    BlockCallback.AFTER_BREAK.register { world, player, pos, state, blockEntity, tool ->
        val item = tool.item
        if (item !is FairyToolItem) return@register
        item.onAfterBreakBlock(world, player, pos, state, blockEntity, tool)
    }

    ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
        val attacker = damageSource.entity as? LivingEntity ?: return@register
        val item = attacker.mainHandItem.item
        if (item !is FairyToolItem) return@register
        item.onKilled(entity, attacker, damageSource)
    }

    AreaMiningToolEffectType.init()
    MineAllToolEffectType.init()
    CutAllToolEffectType.init()
    SelfMendingToolEffectType.init()
    ObtainFairyToolEffectType.init()
    CollectionToolEffectType.init()
    SoulStreamContainableToolEffectType.init()

}

interface ToolEffectType<T> {
    fun castOrThrow(value: Any?): T
    fun merge(a: T, b: T): T
}

abstract class ToolConfiguration {

    private var initialized = false
    private val effects = mutableMapOf<ToolEffectType<*>, ToolEffectEntry<*>>()

    class ToolEffectEntry<T>(val type: ToolEffectType<T>, val value: T, val delegate: (T) -> Unit)

    fun <T> merge(type: ToolEffectType<T>, value: T, delegate: (T) -> Unit) {
        if (initialized) throw IllegalStateException("ToolConfiguration is already initialized.")
        val entry = effects[type]
        val newValue = if (entry == null) value else type.merge(type.castOrThrow(entry.value), value)
        effects[type] = ToolEffectEntry(type, newValue, delegate)
    }

    fun apply() {
        if (initialized) throw IllegalStateException("ToolConfiguration is already initialized.")
        initialized = true
        effects.forEach {
            fun <T> f(entry: ToolEffectEntry<T>) {
                entry.delegate(entry.value)
            }
            f(it.value)
        }
    }


    abstract val toolMaterialCard: ToolMaterialCard
    val tags = mutableListOf<TagKey<Item>>()
    var miningSpeedMultiplierOverride: Float? = null
    val superEffectiveBlocks = mutableListOf<Block>()
    val effectiveBlocks = mutableListOf<Block>()
    val effectiveBlockTags = mutableListOf<TagKey<Block>>()
    var miningDamage = 1.0
    val descriptions = mutableListOf<Text>()
    var hasGlint = false

    val onPostMineListeners = mutableListOf<(item: Item, stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity) -> Unit>()
    val onAfterBreakBlockListeners = mutableListOf<(item: Item, world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) -> Unit>()
    val onKilledListeners = mutableListOf<(item: Item, entity: LivingEntity, attacker: LivingEntity, damageSource: DamageSource) -> Unit>()
    val onInventoryTickListeners = mutableListOf<(item: Item, stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) -> Unit>()
    val onOverrideEnchantmentLevelListeners = mutableListOf<(item: Item, enchantment: Enchantment, old: Int) -> Int>()
    val onConvertItemStackListeners = mutableListOf<(item: Item, itemStack: ItemStack) -> ItemStack>()

    abstract fun createItem(): Item

    context(ModContext)
    fun init(card: ToolCard) {
        tags.forEach {
            card.item.registerItemTagGeneration { it }
        }
        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }

    fun appendPoems(poemList: PoemList): PoemList {
        return descriptions.fold(poemList) { it, description -> it.text(PoemType.DESCRIPTION, description) }
    }

}

abstract class FairyMiningToolConfiguration : ToolConfiguration() {
    var attackDamage = 0F
    var attackSpeed = 0F
}
