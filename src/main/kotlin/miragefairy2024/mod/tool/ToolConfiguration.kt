package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.BlockCallback
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.text
import miragefairy2024.mod.tool.items.FairyToolItem
import miragefairy2024.mod.tool.items.onAfterBreakBlock
import miragefairy2024.mod.tool.items.onKilled
import miragefairy2024.util.Translation
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.ceilToInt
import mirrg.kotlin.hydrogen.max
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

context(ModContext)
fun initToolConfiguration() {

    ToolConfiguration.AREA_MINING_TRANSLATION.enJa()
    ToolConfiguration.MINE_ALL_TRANSLATION.enJa()
    ToolConfiguration.CUT_ALL_TRANSLATION.enJa()
    ToolConfiguration.SELF_MENDING_TRANSLATION.enJa()
    ToolConfiguration.OBTAIN_FAIRY_TRANSLATION.enJa()
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

}

interface ToolEffectType<T : Any> {
    fun castOrThrow(value: Any): T
    fun merge(a: T, b: T): T
    fun init(value: T)
}

abstract class ToolConfiguration {
    companion object {
        private val identifier = MirageFairy2024.identifier("fairy_mining_tool")
        val AREA_MINING_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.area_mining" }, "Area mining %s", "範囲採掘 %s")
        val MINE_ALL_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")
        val CUT_ALL_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.cut_all" }, "Cut down the entire tree", "木全体を伐採")
        val SELF_MENDING_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.self_mending" }, "Self-mending while in the main hand", "メインハンドにある間、自己修繕")
        val OBTAIN_FAIRY_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.obtain_fairy_when_mined" }, "Obtain a fairy when mined or killed", "採掘・撃破時に妖精を入手")
        val COLLECTION_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.collection" }, "Collect drop items when mined or killed", "採掘・撃破時にドロップ品を回収")
    }


    private var initialized = false
    private val effects = mutableMapOf<ToolEffectType<*>, Any>()

    operator fun <T : Any> set(effectType: ToolEffectType<T>, value: T) {
        effects[effectType] = value
    }

    operator fun <T : Any> get(effectType: ToolEffectType<T>): T? {
        val value = effects[effectType] ?: return null
        return effectType.castOrThrow(value)
    }

    fun <T : Any> merge(effectType: ToolEffectType<T>, value: T) {
        if (initialized) throw IllegalStateException("ToolConfiguration is already initialized.")

        val old = this[effectType]
        if (old == null) {
            this[effectType] = value
            return
        }
        this[effectType] = effectType.merge(old, value)
    }

    fun init() {
        if (initialized) throw IllegalStateException("ToolConfiguration is already initialized.")
        initialized = true
        effects.forEach {
            fun <T : Any> f(toolEffectType: ToolEffectType<T>) {
                val value = effects[toolEffectType] ?: return
                toolEffectType.init(toolEffectType.castOrThrow(value))
            }
            f(it.key)
        }
    }


    abstract val toolMaterialCard: ToolMaterialCard
    val tags = mutableListOf<TagKey<Item>>()
    var miningSpeedMultiplierOverride: Float? = null
    val superEffectiveBlocks = mutableListOf<Block>()
    val effectiveBlocks = mutableListOf<Block>()
    val effectiveBlockTags = mutableListOf<TagKey<Block>>()
    var miningDamage = 1.0
    var mineAll = false
    var cutAll = false
    var selfMending: Int? = null
    val descriptions = mutableListOf<Text>()
    var obtainFairy: Double? = null
    var collection = false
    var hasGlint = false

    val onAddPoemListeners = mutableListOf<(item: Item, poemList: PoemList) -> PoemList>()
    val onPostMineListeners = mutableListOf<(item: Item, stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity) -> Unit>()
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


object AreaMiningToolEffectType : ToolEffectType<AreaMiningToolEffectType.Value> {
    class Value(val configuration: ToolConfiguration, val level: Int)

    override fun castOrThrow(value: Any) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.configuration, a.level max b.level)
    override fun init(value: Value) {
        value.configuration.onAddPoemListeners += { _, poemList ->
            if (value.level > 0) {
                poemList.text(PoemType.DESCRIPTION, text { ToolConfiguration.AREA_MINING_TRANSLATION(value.level.toRomanText()) })
            } else {
                poemList
            }
        }
        value.configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (value.level <= 0) return@fail
            areaMining(item, stack, world, state, pos, miner, value)
        }
    }

    private fun areaMining(item: Item, stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity, value: Value) {
        run fail@{
            if (world.isClient) return@fail

            if (miner.isSneaking) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!item.isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない

            // 発動

            val baseHardness = state.getHardness(world, pos)

            // TODO 貫通抑制
            (-value.level..value.level).forEach { x ->
                (-value.level..value.level).forEach { y ->
                    (-value.level..value.level).forEach { z ->
                        if (x != 0 || y != 0 || z != 0) {
                            val targetBlockPos = pos.add(x, y, z)
                            if (item.isSuitableFor(world.getBlockState(targetBlockPos))) run skip@{
                                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                                if (stack.maxDamage - stack.damage <= value.configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                                // 採掘を続行

                                val targetBlockState = world.getBlockState(targetBlockPos)
                                val targetHardness = targetBlockState.getHardness(world, targetBlockPos)
                                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                                if (breakBlockByMagic(stack, world, targetBlockPos, miner)) {
                                    if (targetHardness > 0) {
                                        val damage = world.random.randomInt(value.configuration.miningDamage)
                                        if (damage > 0) {
                                            stack.damage(damage, miner) {
                                                it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun ToolConfiguration.areaMining(areaMining: Int = 1) = this.also {
    this.merge(AreaMiningToolEffectType, AreaMiningToolEffectType.Value(this, areaMining))
}

fun ToolConfiguration.mineAll() = this.also {
    it.mineAll = true
    it.descriptions += text { ToolConfiguration.MINE_ALL_TRANSLATION() }
}

fun ToolConfiguration.cutAll() = this.also {
    it.cutAll = true
    it.descriptions += text { ToolConfiguration.CUT_ALL_TRANSLATION() }
}

object EnchantmentToolEffectType : ToolEffectType<EnchantmentToolEffectType.Value> {
    class Value(val configuration: ToolConfiguration, val map: Map<Enchantment, Int>)

    override fun castOrThrow(value: Any) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.configuration, (a.map.keys + b.map.keys).associateWith { key -> (a.map[key] ?: 0) max (b.map[key] ?: 0) })
    override fun init(value: Value) {
        value.configuration.onAddPoemListeners += { _, poemList ->
            value.map.entries.fold(poemList) { poemList2, (enchantment, level) ->
                poemList2.text(PoemType.DESCRIPTION, text { translate(enchantment.translationKey) + if (level >= 2 || enchantment.maxLevel >= 2) " "() + level.toRomanText() else ""() })
            }
        }
        value.configuration.onOverrideEnchantmentLevelListeners += fail@{ _, enchantment, oldLevel ->
            val newLevel = value.map[enchantment] ?: return@fail oldLevel
            oldLevel max newLevel
        }
        value.configuration.onConvertItemStackListeners += { _, itemStack ->
            var itemStack2 = itemStack
            if ((value.map[Enchantments.SILK_TOUCH] ?: 0) >= 1) {
                itemStack2 = itemStack2.copy()
                val enchantments = EnchantmentHelper.get(itemStack2)
                enchantments[Enchantments.SILK_TOUCH] = (enchantments[Enchantments.SILK_TOUCH] ?: 0) atLeast 1
                EnchantmentHelper.set(enchantments, itemStack2)
            }
            itemStack2
        }
    }
}

fun ToolConfiguration.enchantment(enchantment: Enchantment, level: Int = 1) = this.also {
    this.merge(EnchantmentToolEffectType, EnchantmentToolEffectType.Value(this, mapOf(enchantment to level)))
}

fun ToolConfiguration.selfMending(selfMending: Int) = this.also {
    it.selfMending = selfMending
    it.descriptions += text { ToolConfiguration.SELF_MENDING_TRANSLATION() }
}

fun ToolConfiguration.obtainFairy(appearanceRateBonus: Double) = this.also {
    it.obtainFairy = appearanceRateBonus
    it.descriptions += text { ToolConfiguration.OBTAIN_FAIRY_TRANSLATION() }
}

fun ToolConfiguration.collection() = this.also {
    it.collection = true
    it.descriptions += text { ToolConfiguration.COLLECTION_TRANSLATION() }
}

fun ToolConfiguration.glint() = this.also {
    it.hasGlint = true
}
