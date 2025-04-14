package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.mod.fairy.SoulStream
import miragefairy2024.mod.fairy.contains
import miragefairy2024.mod.fairy.soulStream
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.sync
import miragefairy2024.util.Translation
import miragefairy2024.util.compound
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.Slot
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.resources.ResourceLocation as Identifier
import kotlin.math.log

private val identifier = MirageFairy2024.identifier("passive_skill")
val PASSIVE_SKILL_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}" }, "Passive Skills", "パッシブスキル")
val PASSIVE_SKILL_DISABLED_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.disabled" }, "Outside target slot", "対象スロット外")
val PASSIVE_SKILL_OVERFLOWED_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.overflowed" }, "Too many passive skills!", "パッシブスキルが多すぎます！")
val PASSIVE_SKILL_SUPPORTING_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.supporting" }, "Supporting other item", "他のアイテムを支援中")
val PASSIVE_SKILL_EFFECTIVE_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.effective" }, "Effective", "発動中")

context(ModContext)
fun initPassiveSkillExecution() {

    // イベント処理
    ServerTickEvents.END_SERVER_TICK.register { server ->
        if (server.ticks % 20 == 0) {
            server.playerManager.playerList.forEach { player ->

                // 現在装備しているパッシブスキルの列挙
                val passiveSkillProviders = player.findPassiveSkillProviders()

                // 現在発動しているパッシブスキル効果の計算
                val result = PassiveSkillResult()
                result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true) // 先行判定
                val manaBoostValue = result[ManaBoostPassiveSkillEffect]
                result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false) // 後行判定

                // 効果
                result.update(player)
                PassiveSkillResultExtraPlayerDataCategory.sync(player)

            }
        }
    }

    // パッシブスキル更新時に使われる古いデータをプレイヤーに保存する
    PassiveSkillResultExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, MirageFairy2024.identifier("passive_skill_result"))

    // 翻訳
    PASSIVE_SKILL_TRANSLATION.enJa()
    PASSIVE_SKILL_DISABLED_TRANSLATION.enJa()
    PASSIVE_SKILL_OVERFLOWED_TRANSLATION.enJa()
    PASSIVE_SKILL_SUPPORTING_TRANSLATION.enJa()
    PASSIVE_SKILL_EFFECTIVE_TRANSLATION.enJa()

}

enum class PassiveSkillStatus {
    /** アイテムが有効なスロットにありません。 */
    DISABLED,

    /** パッシブスキルがあふれています。 */
    OVERFLOWED,

    /** 同種のアイテムを支援中。 */
    SUPPORTING,

    /** パッシブスキルは有効です。 */
    EFFECTIVE,
}

val PassiveSkillStatus.description
    get() = when (this) {
        PassiveSkillStatus.DISABLED -> text { PASSIVE_SKILL_DISABLED_TRANSLATION() }
        PassiveSkillStatus.OVERFLOWED -> text { PASSIVE_SKILL_OVERFLOWED_TRANSLATION() }
        PassiveSkillStatus.SUPPORTING -> text { PASSIVE_SKILL_SUPPORTING_TRANSLATION() }
        PassiveSkillStatus.EFFECTIVE -> text { PASSIVE_SKILL_EFFECTIVE_TRANSLATION() }
    }

class PassiveSkillProviders(val providers: List<Triple<ItemStack, PassiveSkillStatus, PassiveSkill>>, val passiveSkills: List<PassiveSkill>)

@Suppress("UnusedReceiverParameter")
fun PlayerEntity.getPassiveSkillCount() = 9

fun PlayerEntity.findPassiveSkillProviders(): PassiveSkillProviders {
    val passiveSkillCount = this.getPassiveSkillCount()

    val providers = mutableListOf<Triple<ItemStack, PassiveSkillStatus, Slot<PassiveSkill>>>()
    val passiveSkillSlotListTable = mutableMapOf<Identifier, MutableList<Slot<PassiveSkill>>>()

    fun addItemStack(itemStack: ItemStack, enabled: Boolean) {
        val item = itemStack.item
        if (item !is PassiveSkillProvider) return
        val passiveSkill = item.getPassiveSkill(itemStack) ?: return

        val passiveSkills = passiveSkillSlotListTable[passiveSkill.providerId]
        if (passiveSkills != null) { // 既存パッシブスキル
            // 他のアイテムを支援
            val slot = Slot(passiveSkill)
            providers += Triple(itemStack, PassiveSkillStatus.SUPPORTING, slot)
            passiveSkills += slot
        } else { // 新規パッシブスキル
            if (!enabled) { // 発動対象スロットでない場所に配置されている
                // 発動対象でないため新規パッシブスキルを発動しない
            } else { // 発動対象スロットに配置されている
                // パッシブスキルを新しく発動しようとしている
                if (passiveSkillSlotListTable.size >= passiveSkillCount) { // パッシブスキルの枠が満杯
                    // パッシブスキルがあふれた
                    providers += Triple(itemStack, PassiveSkillStatus.OVERFLOWED, Slot(passiveSkill))
                } else { // パッシブスキルの枠に余裕がある
                    // パッシブスキルを新しく発動する
                    val slot = Slot(passiveSkill)
                    providers += Triple(itemStack, PassiveSkillStatus.EFFECTIVE, slot)
                    passiveSkillSlotListTable[passiveSkill.providerId] = mutableListOf(slot)
                }
            }
        }
    }

    // アイテムを検出
    addItemStack(this.offHandStack, true)
    this.armorItems.forEach {
        addItemStack(it, true)
    }
    repeat(SoulStream.SLOT_COUNT) { index ->
        addItemStack(this.soulStream[index], index < SoulStream.PASSIVE_SKILL_SLOT_COUNT)
    }

    // パッシブスキルを統合
    val passiveSkills = passiveSkillSlotListTable.values.map { passiveSkillSlotList ->
        val mainPassiveSkillSlot = passiveSkillSlotList[0]
        val passiveSkill = PassiveSkill(
            mainPassiveSkillSlot.value.providerId,
            mainPassiveSkillSlot.value.motif,
            mainPassiveSkillSlot.value.rare,
            passiveSkillSlotList.sumOf { it.value.count },
            mainPassiveSkillSlot.value.specifications,
        )
        passiveSkillSlotList.forEach {
            it.value = passiveSkill
        }
        passiveSkill
    }
    val actualProviders = providers.map { Triple(it.first, it.second, it.third.value) }

    return PassiveSkillProviders(actualProviders, passiveSkills)
}

fun PassiveSkillResult.collect(passiveSkills: Iterable<PassiveSkill>, player: PlayerEntity, manaBoostValue: ManaBoostPassiveSkillEffect.Value, isPreprocessing: Boolean) {
    val context = PassiveSkillContext(player.world, player.eyeBlockPos, player)

    passiveSkills.forEach { passiveSkill ->
        val motif = passiveSkill.motif
        val level = passiveSkill.rare + log(passiveSkill.count, 3.0)
        val mana = level * (1.0 + manaBoostValue.map.entries.sumOf { (keyMotif, value) -> if (motif in keyMotif) value else 0.0 })
        passiveSkill.specifications.forEach { specification ->
            fun <T> f(specification: PassiveSkillSpecification<T>) {
                if (specification.effect.isPreprocessor == isPreprocessing) {
                    if (specification.conditions.all { it.test(context, level, mana) }) {
                        this.add(specification.effect, specification.valueProvider(mana))
                    }
                }
            }
            f(specification)
        }
    }
}

fun PassiveSkillResult.update(player: PlayerEntity) {
    val context = PassiveSkillContext(player.world, player.eyeBlockPos, player)

    val oldResult = player.passiveSkillResult
    player.passiveSkillResult = this

    passiveSkillEffectRegistry.entrySet().forEach {
        fun <T> f(type: PassiveSkillEffect<T>) {
            val oldValue = oldResult[type]
            val newValue = this[type]
            type.update(context, oldValue, newValue)
        }
        f(it.value)
    }
}


// PassiveSkillResult

object PassiveSkillResultExtraPlayerDataCategory : ExtraPlayerDataCategory<PassiveSkillResult> {
    override fun create() = PassiveSkillResult()
    override fun castOrThrow(value: Any) = value as PassiveSkillResult

    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<PassiveSkillResult> {
        override fun fromNbt(nbt: NbtCompound): PassiveSkillResult {
            val result = PassiveSkillResult()
            nbt.keys.forEach { key ->
                fun <T> f(passiveSkillEffect: PassiveSkillEffect<T>) {
                    result.map[passiveSkillEffect] = passiveSkillEffect.fromNbt(nbt[key] as NbtCompound)
                }

                val passiveSkillEffect = passiveSkillEffectRegistry.get(key.toIdentifier()) ?: return@forEach
                f(passiveSkillEffect)
            }
            return result
        }

        override fun toNbt(data: PassiveSkillResult): NbtCompound {
            val nbt = NbtCompound()
            data.map.entries.forEach {
                fun <T> f(passiveSkillEffect: PassiveSkillEffect<T>) {
                    val identifier = passiveSkillEffectRegistry.getKey(passiveSkillEffect) ?: return
                    nbt.wrapper[identifier.string].compound.set(passiveSkillEffect.toNbt(passiveSkillEffect.castOrThrow(it.value)))
                }

                f(it.key)
            }
            return nbt
        }
    }
}

class PassiveSkillResult {
    val map = mutableMapOf<PassiveSkillEffect<*>, Any?>()

    operator fun <T> get(type: PassiveSkillEffect<T>) = if (type in map) type.castOrThrow(map[type]) else type.unit

    fun <T> add(type: PassiveSkillEffect<T>, value: T) {
        if (type in map) {
            map[type] = type.combine(type.castOrThrow(map[type]), value)
        } else {
            map[type] = value
        }
    }
}

var PlayerEntity.passiveSkillResult
    get() = this.extraPlayerDataContainer.getOrInit(PassiveSkillResultExtraPlayerDataCategory)
    set(value) {
        this.extraPlayerDataContainer[PassiveSkillResultExtraPlayerDataCategory] = value
    }
