package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.SoundEventCard
import miragefairy2024.mod.invoke
import miragefairy2024.util.Translation
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.buildText
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import java.util.UUID

fun initPassiveSkillEffects() {
    PassiveSkillEffectCard.entries.forEach { card ->
        card.register(passiveSkillEffectRegistry, card.identifier)
        card.init()
    }

    EntityAttributePassiveSkillEffect.formatters[EntityAttributes.GENERIC_MOVEMENT_SPEED] = { (it / 0.1) * 100 formatAs "%+.0f%%" }
}

abstract class PassiveSkillEffectCard<T>(path: String) : PassiveSkillEffect<T> {
    companion object {
        val entries = mutableListOf<PassiveSkillEffectCard<*>>()
        private operator fun <T> PassiveSkillEffectCard<T>.unaryPlus() = this.also { entries += it }

        val MANA_BOOST = +ManaBoostPassiveSkillEffect
        val ENTITY_ATTRIBUTE = +EntityAttributePassiveSkillEffect
        val STATUS_EFFECT = +StatusEffectPassiveSkillEffect
        val IGNITION = +IgnitionPassiveSkillEffect
        val EXPERIENCE = +ExperiencePassiveSkillEffect
        val REGENERATION = +RegenerationPassiveSkillEffect
        val MENDING = +MendingPassiveSkillEffect
        val COLLECTION = +CollectionPassiveSkillEffect
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    open fun init() = Unit

    override val isPreprocessor = false
}

// TODO 条件付き魔力パッシブ
object ManaBoostPassiveSkillEffect : PassiveSkillEffectCard<Double>("mana_boost") {
    override val isPreprocessor = true
    val translation = Translation({ "miragefairy2024.passive_skill_type.${identifier.toTranslationKey()}" }, "Mana", "魔力")
    override fun getText(value: Double) = text { translation() + " "() + Emoji.MANA() + (value * 100 formatAs "%+.0f%%")() }
    override val unit = 0.0
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) = Unit
    override fun init() {
        translation.enJa()
    }
}

object EntityAttributePassiveSkillEffect : PassiveSkillEffectCard<EntityAttributePassiveSkillEffect.Value>("entity_attribute") {
    val formatters = mutableMapOf<EntityAttribute, (Double) -> String>()
    private val defaultFormatter: (Double) -> String = { it formatAs "%+.2f" }
    private val uuid: UUID = UUID.fromString("AEC6063C-2320-4FAC-820D-0562438ECAAC")

    class Value(val map: Map<EntityAttribute, Double>)

    override fun getText(value: Value): Text {
        return value.map.map { (attribute, value) ->
            text { translate(attribute.translationKey) + " ${formatters.getOrElse(attribute) { defaultFormatter }(value)}"() }
        }.join(text { ","() })
    }

    override val unit = Value(mapOf())
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (attribute, value) ->
            map[attribute] = map.getOrElse(attribute) { 0.0 } + value
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) {

        // 削除するべきものを削除
        oldValue.map.forEach { (attribute, _) ->
            val customInstance = context.player.attributes.getCustomInstance(attribute) ?: return@forEach
            if (attribute !in newValue.map) {
                customInstance.removeModifier(uuid)
            }
        }

        // 追加および変更
        newValue.map.forEach { (attribute, value) ->
            val customInstance = context.player.attributes.getCustomInstance(attribute) ?: return@forEach
            val oldModifier = customInstance.getModifier(uuid)
            if (oldModifier == null) {
                val modifier = EntityAttributeModifier(uuid, "Fairy Bonus", value, EntityAttributeModifier.Operation.ADDITION)
                customInstance.addTemporaryModifier(modifier)
            } else if (oldModifier.value != value) {
                customInstance.removeModifier(uuid)
                val modifier = EntityAttributeModifier(uuid, "Fairy Bonus", value, EntityAttributeModifier.Operation.ADDITION)
                customInstance.addTemporaryModifier(modifier)
            }
        }

    }
}

object StatusEffectPassiveSkillEffect : PassiveSkillEffectCard<StatusEffectPassiveSkillEffect.Value>("status_effect") {
    class Value(val map: Map<StatusEffect, Entry>)
    class Entry(val level: Int, val additionalSeconds: Int)

    override fun getText(value: Value): Text {
        return value.map.map { (statusEffect, entry) ->
            buildText {
                !statusEffect.name
                if (entry.level in 2..10) !(" "() + translate("enchantment.level.${entry.level}"))
            }
        }.join(text { ","() })
    }

    override val unit = Value(mapOf())
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (statusEffect, bEntry) ->
            val aEntry = map[statusEffect]
            if (aEntry == null || aEntry.level < bEntry.level || aEntry.additionalSeconds < bEntry.additionalSeconds) {
                map[statusEffect] = bEntry
            }
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) {
        newValue.map.forEach { (statusEffect, entry) ->
            context.player.addStatusEffect(StatusEffectInstance(statusEffect, 20 * (1 + 1 + entry.additionalSeconds), entry.level - 1, true, false, true))
        }
    }
}

object IgnitionPassiveSkillEffect : PassiveSkillEffectCard<Boolean>("ignition") {
    val translation = Translation({ "miragefairy2024.passive_skill_type.${identifier.toTranslationKey()}" }, "Ignition", "発火")
    override fun getText(value: Boolean) = text { if (value) translation() else empty() }
    override val unit = false
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun combine(a: Boolean, b: Boolean) = a || b
    override fun update(context: PassiveSkillContext, oldValue: Boolean, newValue: Boolean) {
        if (newValue) {
            if (context.player.isWet || context.player.inPowderSnow || context.player.wasInPowderSnow) return
            context.player.fireTicks = 30 atLeast context.player.fireTicks
        }
    }

    override fun init() {
        translation.enJa()
    }
}

abstract class DoublePassiveSkillEffectCard(path: String) : PassiveSkillEffectCard<Double>(path) {
    override val unit = 0.0
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
}

object ExperiencePassiveSkillEffect : DoublePassiveSkillEffectCard("experience") {
    val translation = Translation({ "miragefairy2024.passive_skill_type.${identifier.toTranslationKey()}" }, "Gain XP: %s/s", "経験値獲得: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue > 0.0) {
            val actualAmount = context.world.random.randomInt(newValue)
            if (actualAmount > 0) {
                context.player.addExperience(actualAmount)
            }
        }
    }

    override fun init() {
        translation.enJa()
    }
}

object RegenerationPassiveSkillEffect : DoublePassiveSkillEffectCard("regeneration") {
    val translation = Translation({ "miragefairy2024.passive_skill_type.${identifier.toTranslationKey()}" }, "Regeneration: %s/s", "持続回復: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue > 0.0) {
            if (context.player.health < context.player.maxHealth) {
                context.player.heal(newValue.toFloat())
            }
        }
    }

    override fun init() {
        translation.enJa()
    }
}

object MendingPassiveSkillEffect : DoublePassiveSkillEffectCard("mending") {
    val translation = Translation({ "miragefairy2024.passive_skill_type.${identifier.toTranslationKey()}" }, "Mending: %s/s", "修繕: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        val repairPower = context.world.random.randomInt(newValue)
        if (repairPower <= 0) return
        val itemStack = context.player.mainHandStack
        if (!itemStack.isDamageable) return
        val actualRepairAmount = repairPower atMost itemStack.damage
        if (actualRepairAmount <= 0) return
        itemStack.damage -= actualRepairAmount
    }

    override fun init() {
        translation.enJa()
    }
}

object CollectionPassiveSkillEffect : DoublePassiveSkillEffectCard("collection") {
    val translation = Translation({ "miragefairy2024.passive_skill_type.${identifier.toTranslationKey()}" }, "Collection: %s/s", "収集: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        val world = context.world
        val player = context.player
        if (newValue <= 0.0) return
        val actualAmount = world.random.randomInt(newValue)
        if (actualAmount <= 0) return

        val originalBlockPos = player.eyeBlockPos
        val reach = 15
        val itemEntities = world.getEntitiesByClass(ItemEntity::class.java, Box(originalBlockPos).expand(reach.toDouble())) {
            when {
                it.isSpectator -> false // スペクテイターモードであるアイテムには無反応
                it.boundingBox.intersects(player.boundingBox) -> false // 既に触れているアイテムには無反応
                else -> true
            }
        }

        var remainingAmount = actualAmount
        var processedCount = 0
        run finish@{
            blockVisitor(listOf(originalBlockPos), maxDistance = reach) { fromBlockPos, toBlockPos ->
                val offset = toBlockPos.subtract(fromBlockPos)
                val direction = when {
                    offset.y == -1 -> Direction.DOWN
                    offset.y == 1 -> Direction.UP
                    offset.z == -1 -> Direction.NORTH
                    offset.z == 1 -> Direction.SOUTH
                    offset.x == -1 -> Direction.WEST
                    offset.x == 1 -> Direction.EAST
                    else -> throw AssertionError()
                }
                !world.getBlockState(fromBlockPos).isSideSolidFullSquare(world, fromBlockPos, direction) && !world.getBlockState(toBlockPos).isSideSolidFullSquare(world, toBlockPos, direction.opposite)
            }.forEach { (_, blockPos) ->
                val currentBox = Box(blockPos).expand(0.98, 0.0, 0.98)
                itemEntities
                    .filter { it.boundingBox.intersects(currentBox) }
                    .forEach {

                        it.teleport(player.x, player.y, player.z)
                        it.resetPickupDelay()

                        processedCount++

                        remainingAmount--
                        if (remainingAmount <= 0) return@finish

                    }
            }
        }

        if (processedCount > 0) {

            // Effect
            world.playSound(null, player.x, player.y, player.z, SoundEventCard.COLLECT.soundEvent, SoundCategory.PLAYERS, 0.15F, 0.8F + (world.random.nextFloat() - 0.5F) * 0.5F)

        }

    }

    override fun init() {
        translation.enJa()
    }
}
