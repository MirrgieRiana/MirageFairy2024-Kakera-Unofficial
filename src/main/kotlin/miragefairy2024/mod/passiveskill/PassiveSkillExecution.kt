package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.mod.fairy.SoulStream
import miragefairy2024.mod.fairy.soulStream
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

val PASSIVE_SKILL_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill" }, "Passive Skills", "パッシブスキル")
val PASSIVE_SKILL_DISABLED_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill.disabled" }, "Outside target slot", "対象スロット外")
val PASSIVE_SKILL_DUPLICATED_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill.duplicated" }, "Duplicate items!", "アイテムが重複！")
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
                result.collect(passiveSkillProviders.passiveSkills, player, 0.0, true) // 先行判定
                val manaBoost = result[PassiveSkillEffectCard.MANA_BOOST]
                result.collect(passiveSkillProviders.passiveSkills, player, manaBoost, false) // 後行判定

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
    PASSIVE_SKILL_DUPLICATED_TRANSLATION.enJa()
    PASSIVE_SKILL_EFFECTIVE_TRANSLATION.enJa()

}

enum class PassiveSkillStatus {
    /** アイテムが有効なスロットにありません。 */
    DISABLED,

    /** 同種のアイテムが既に存在します。 */
    DUPLICATED,

    /** パッシブスキルは有効です。 */
    EFFECTIVE,
}

val PassiveSkillStatus.description
    get() = when (this) {
        PassiveSkillStatus.DISABLED -> PASSIVE_SKILL_DISABLED_TRANSLATION()
        PassiveSkillStatus.DUPLICATED -> PASSIVE_SKILL_DUPLICATED_TRANSLATION()
        PassiveSkillStatus.EFFECTIVE -> PASSIVE_SKILL_EFFECTIVE_TRANSLATION()
    }

class PassiveSkillProviders(val providers: List<Pair<ItemStack, PassiveSkillStatus>>, val passiveSkills: List<PassiveSkill>)

fun PlayerEntity.findPassiveSkillProviders(): PassiveSkillProviders {
    val providers = mutableListOf<Pair<ItemStack, PassiveSkillStatus>>()
    val passiveSkills = mutableListOf<PassiveSkill>()
    val acceptedProviderIds = mutableSetOf<Identifier>()
    val soulStream = this.soulStream
    SoulStream.PASSIVE_SKILL_SLOT_INDICES.forEach { slotIndex ->
        val itemStack = soulStream[slotIndex]
        val item = itemStack.item
        if (item is PassiveSkillProvider) {
            val passiveSkill = item.getPassiveSkill(itemStack)
            if (passiveSkill != null) {
                if (passiveSkill.providerId in acceptedProviderIds) {
                    providers += Pair(itemStack, PassiveSkillStatus.DUPLICATED)
                } else {
                    acceptedProviderIds += passiveSkill.providerId
                    providers += Pair(itemStack, PassiveSkillStatus.EFFECTIVE)
                    passiveSkills += passiveSkill
                }
            }
        }
    }
    return PassiveSkillProviders(providers.toList(), passiveSkills.toList())
}

fun PassiveSkillResult.collect(passiveSkills: Iterable<PassiveSkill>, player: PlayerEntity, manaBoost: Double, isPreprocessing: Boolean) {
    val context = PassiveSkillContext(player.world, player.eyeBlockPos, player)

    passiveSkills.forEach { passiveSkill ->
        val mana = passiveSkill.itemStackMana * (1.0 + manaBoost)
        passiveSkill.specifications.forEach { specification ->
            fun <T> f(specification: PassiveSkillSpecification<T>) {
                if (specification.effect.isPreprocessor == isPreprocessing) {
                    if (specification.conditions.all { it.test(context, mana) }) {
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
