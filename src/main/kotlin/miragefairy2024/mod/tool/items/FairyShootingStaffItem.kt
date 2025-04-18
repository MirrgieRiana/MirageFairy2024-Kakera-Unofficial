package miragefairy2024.mod.tool.items

import miragefairy2024.MirageFairy2024
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.EnchantmentCard
import miragefairy2024.mod.SoundEventCard
import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.AntimatterBoltEntity
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.Translation
import miragefairy2024.util.getLevel
import miragefairy2024.util.getRate
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TieredItem as ToolItem
import net.minecraft.world.item.Tier as ToolMaterial
import net.minecraft.world.item.Vanishable
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.stats.Stats
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class FairyShootingStaffConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    var basePower: Float,
    var baseMaxDistance: Float,
) : ToolConfiguration() {
    override fun createItem() = FairyShootingStaffItem(this, Item.Properties())
}

class FairyShootingStaffItem(override val configuration: FairyShootingStaffConfiguration, settings: Properties) :
    ShootingStaffItem(configuration.toolMaterialCard.toolMaterial, configuration.basePower, configuration.baseMaxDistance, settings),
    FairyToolItem,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {

    override fun getDestroySpeed(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(stack, state)

    override fun isCorrectToolForDrops(state: BlockState) = isSuitableForImpl(state)

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.mineBlock(stack, world, state, pos, miner)
        postMineImpl(stack, world, state, pos, miner)
        return true
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.hurtEnemy(stack, target, attacker)
        postHitImpl(stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(stack, world, entity, slot, selected)
    }

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = overrideEnchantmentLevelImpl(enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(itemStack)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}

open class ShootingStaffItem(toolMaterial: ToolMaterial, private val basePower: Float, private val baseMaxDistance: Float, settings: Properties) : ToolItem(toolMaterial, settings), Vanishable {
    companion object {
        val NOT_ENOUGH_EXPERIENCE_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_tool_item").toLanguageKey()}.not_enough_experience" }, "Not enough experience", "経験値が足りません")
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("shooting_staff").toLanguageKey()}.description" }, "Perform a ranged attack when used", "使用時、射撃攻撃")
        const val BASE_EXPERIENCE_COST = 2
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { DESCRIPTION_TRANSLATION().yellow }
    }

    override fun use(world: Level, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getItemInHand(hand)
        if (world.isClientSide) return TypedActionResult.success(itemStack)

        val damage = basePower + 0.5F * EnchantmentCard.MAGIC_POWER.enchantment.getLevel(itemStack).toFloat()
        val maxDistance = baseMaxDistance + 3F * EnchantmentCard.MAGIC_REACH.enchantment.getLevel(itemStack)
        val speed = 2.0F + 2.0F * EnchantmentCard.MAGIC_REACH.enchantment.getRate(itemStack).toFloat()
        val frequency = 0.5 + 0.5 * EnchantmentCard.MAGIC_ACCELERATION.enchantment.getRate(itemStack)
        val experienceCost = BASE_EXPERIENCE_COST + 1 * EnchantmentCard.MAGIC_POWER.enchantment.getLevel(itemStack)

        if (!user.isCreative) {
            if (user.totalExperience < experienceCost) {
                user.displayClientMessage(text { NOT_ENOUGH_EXPERIENCE_TRANSLATION() }, true)
                return TypedActionResult.consume(itemStack)
            }
        }

        // 生成
        val entity = AntimatterBoltEntity(AntimatterBoltCard.entityType, world)
        entity.setPos(user.x, user.eyeY - 0.3, user.z)
        entity.shootFromRotation(user, user.xRot, user.yRot, 0.0F, speed, 1.0F)
        entity.owner = user
        entity.damage = damage
        entity.maxDistance = maxDistance
        world.addFreshEntity(entity)

        // 消費
        itemStack.hurtAndBreak(1, user) {
            it.broadcastBreakEvent(hand)
        }
        if (!user.isCreative) user.giveExperiencePoints(-experienceCost)

        user.cooldowns.addCooldown(this, world.random.randomInt(10.0 / frequency))

        // 統計
        user.awardStat(Stats.ITEM_USED.get(this))

        // エフェクト
        world.playSound(null, user.x, user.y, user.z, SoundEventCard.MAGIC2.soundEvent, SoundCategory.PLAYERS, 0.6F, 0.90F + (world.random.nextFloat() - 0.5F) * 0.3F)

        return TypedActionResult.consume(itemStack)
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        stack.hurtAndBreak(2, attacker) { e ->
            e.broadcastBreakEvent(EquipmentSlot.MAINHAND)
        }
        return true
    }

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        if (state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(2, miner) { e ->
                e.broadcastBreakEvent(EquipmentSlot.MAINHAND)
            }
        }
        return true
    }
}
