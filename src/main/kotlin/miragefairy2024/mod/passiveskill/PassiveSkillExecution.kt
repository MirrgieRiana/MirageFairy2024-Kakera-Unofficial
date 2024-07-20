package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.mod.fairy.SoulStream
import miragefairy2024.mod.fairy.contains
import miragefairy2024.mod.fairy.soulStream
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import mirrg.kotlin.hydrogen.Slot
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import kotlin.math.log

val PASSIVE_SKILL_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill" }, "Passive Skills", "パッシブスキル")
val PASSIVE_SKILL_DISABLED_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill.disabled" }, "Outside target slot", "対象スロット外")
val PASSIVE_SKILL_OVERFLOWED_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill.overflowed" }, "Too many passive skills!", "パッシブスキルが多すぎます！")
val PASSIVE_SKILL_SUPPORTING_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill.supporting" }, "Supporting other item", "他のアイテムを支援中")
val PASSIVE_SKILL_EFFECTIVE_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill.effective" }, "Effective", "発動中")

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
                val manaBoostValue = result[PassiveSkillEffectCard.MANA_BOOST]
                result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false) // 後行判定

                // 効果
                result.update(player)

            }
        }
    }

    // パッシブスキル更新時に使われる古いデータをプレイヤーに保存する
    PassiveSkillResultExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, Identifier(MirageFairy2024.modId, "passive_skill_result"))

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
        PassiveSkillStatus.DISABLED -> PASSIVE_SKILL_DISABLED_TRANSLATION()
        PassiveSkillStatus.OVERFLOWED -> PASSIVE_SKILL_OVERFLOWED_TRANSLATION()
        PassiveSkillStatus.SUPPORTING -> PASSIVE_SKILL_SUPPORTING_TRANSLATION()
        PassiveSkillStatus.EFFECTIVE -> PASSIVE_SKILL_EFFECTIVE_TRANSLATION()
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

    passiveSkillEffectRegistry.entrySet.forEach {
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
