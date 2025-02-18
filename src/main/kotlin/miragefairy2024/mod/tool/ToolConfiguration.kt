package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.BlockCallback
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.text
import miragefairy2024.mod.tool.effects.AreaMiningToolEffectType
import miragefairy2024.mod.tool.effects.CutAllToolEffectType
import miragefairy2024.mod.tool.effects.MineAllToolEffectType
import miragefairy2024.mod.tool.effects.ObtainFairyToolEffectType
import miragefairy2024.mod.tool.effects.SelfMendingToolEffectType
import miragefairy2024.mod.tool.items.FairyToolItem
import miragefairy2024.mod.tool.items.onAfterBreakBlock
import miragefairy2024.mod.tool.items.onKilled
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.text
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

context(ModContext)
fun initToolConfiguration() {

    ToolConfiguration.COLLECTION_TRANSLATION.enJa()

    BlockCallback.AFTER_BREAK.register { world, player, pos, state, blockEntity, tool ->
        val item = tool.item
        if (item !is FairyToolItem) return@register
        item.onAfterBreakBlock(world, player, pos, state, blockEntity, tool)
    }

    ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
        val attacker = damageSource.attacker as? LivingEntity ?: return@register
        val item = attacker.mainHandStack.item
        if (item !is FairyToolItem) return@register
        item.onKilled(entity, attacker, damageSource)
    }

    AreaMiningToolEffectType.init()
    MineAllToolEffectType.init()
    CutAllToolEffectType.init()
    SelfMendingToolEffectType.init()
    ObtainFairyToolEffectType.init()

}

interface ToolEffectType<T> {
    fun castOrThrow(value: Any?): T
    fun merge(a: T, b: T): T
}

abstract class ToolConfiguration {
    companion object {
        private val identifier = MirageFairy2024.identifier("fairy_mining_tool")
        val COLLECTION_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.collection" }, "Collect drop items when mined or killed", "採掘・撃破時にドロップ品を回収")
    }


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
    var collection = false
    var hasGlint = false

    val onAddPoemListeners = mutableListOf<(item: Item, poemList: PoemList) -> PoemList>()
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

    fun appendPoems(item: Item, poemList: PoemList): PoemList {
        val texts = mutableListOf<Text>()

        texts += descriptions

        return onAddPoemListeners.fold(texts.fold(poemList) { it, description -> it.text(PoemType.DESCRIPTION, description) }) { it, listener -> listener(item, it) }
    }

}

abstract class FairyMiningToolConfiguration : ToolConfiguration() {
    var attackDamage = 0F
    var attackSpeed = 0F
}


fun ToolConfiguration.collection() = this.also {
    it.collection = true
    it.descriptions += text { ToolConfiguration.COLLECTION_TRANSLATION() }
}

fun ToolConfiguration.glint() = this.also {
    it.hasGlint = true
}
