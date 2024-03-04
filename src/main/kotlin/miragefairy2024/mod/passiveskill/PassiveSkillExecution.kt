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
import miragefairy2024.util.register
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

val PASSIVE_SKILL_TRANSLATION = Translation({ "item.miragefairy2024.fairy.passive_skill" }, "Passive Skills", "パッシブスキル")
fun initPassiveSkillExecution() {

    // イベント処理
    ServerTickEvents.END_SERVER_TICK.register { server ->
        if (server.ticks % 20 == 0) {
            server.playerManager.playerList.forEach { player ->

                // 現在装備しているパッシブスキルの列挙
                val passiveSkills = player.getPassiveSkills()

                // 現在発動しているパッシブスキル効果の計算
                val result = PassiveSkillResult()
                result.collect(passiveSkills, player, 0.0, true) // 先行判定
                val additionalMana = result[PassiveSkillEffectCard.MANA]
                result.collect(passiveSkills, player, additionalMana, false) // 後行判定

                // 効果
                result.update(player)

            }
        }
    }

    // パッシブスキル更新時に使われる古いデータをプレイヤーに保存する
    PassiveSkillResultExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, Identifier(MirageFairy2024.modId, "passive_skill_result"))

    // 翻訳
    PASSIVE_SKILL_TRANSLATION.enJa()

}

fun PlayerEntity.getPassiveSkills(): List<PassiveSkill> {
    val passiveSkills = mutableListOf<PassiveSkill>()
    val soulStream = this.soulStream
    SoulStream.PASSIVE_SKILL_SLOT_INDICES.forEach { slotIndex ->
        val itemStack = soulStream[slotIndex]
        val item = itemStack.item
        if (item is PassiveSkillProvider) {
            val passiveSkill = item.getPassiveSkill(itemStack)
            if (passiveSkill != null) passiveSkills += passiveSkill
        }
    }
    return passiveSkills.toList()
}

fun PassiveSkillResult.collect(passiveSkills: Iterable<PassiveSkill>, player: PlayerEntity, additionalMana: Double, isPreprocessing: Boolean) {
    val world = player.world
    val blockPos = player.eyeBlockPos

    passiveSkills.forEach { passiveSkill ->
        val mana = passiveSkill.itemStackMana + additionalMana
        passiveSkill.specifications.forEach { specification ->
            fun <T> f(specification: PassiveSkillSpecification<T>) {
                if (specification.effect.isPreprocessor == isPreprocessing) {
                    if (specification.conditions.all { it.test(world, blockPos, player, mana) }) {
                        this.add(specification.effect, specification.valueProvider(mana))
                    }
                }
            }
            f(specification)
        }
    }
}

fun PassiveSkillResult.update(player: PlayerEntity) {
    val world = player.world
    val blockPos = player.eyeBlockPos

    val oldResult = player.passiveSkillResult
    player.passiveSkillResult = this

    passiveSkillEffectRegistry.entrySet.forEach {
        fun <T> f(type: PassiveSkillEffect<T>) {
            val oldValue = oldResult[type]
            val newValue = this[type]
            type.update(world, blockPos, player, oldValue, newValue)
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
