package miragefairy2024.mod.materials.item

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.blue
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.obtain
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.stats.Stats
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.advancements.CriteriaTriggers as Criteria
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.util.StringUtil as StringHelper
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.ItemUtils as ItemUsage
import net.minecraft.world.item.UseAnim as UseAction

class DrinkItem(settings: Properties, private val flaming: Int? = null) : Item(settings) {
    companion object {
        val FLAMING_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("drink").toLanguageKey()}.burning" }, "Flaming", "炎上")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        run {
            val foodComponent = stack[DataComponents.FOOD] ?: return@run
            foodComponent.effects.forEach { entry ->
                var text = entry.effect.effect.value().displayName
                if (entry.effect.amplifier > 0) text = text { text + " "() + (entry.effect.amplifier + 1).toRomanText() }
                if (!entry.effect.effect.value().isInstantenous) text = text { text + " (${StringHelper.formatTickDuration(entry.effect.duration, context.tickRate())}"() + ")"() }
                if (entry.probability != 1.0F) text = text { text + " (${entry.probability * 100 formatAs "%.0f"}%)"() }
                text = if (entry.effect.effect.value().isBeneficial) text.blue else text.red
                tooltipComponents += text
            }
        }

        if (flaming != null) tooltipComponents += text { (FLAMING_TRANSLATION() + " (${StringHelper.formatTickDuration(flaming * 20, context.tickRate())}"() + ")"()).red }
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        super.finishUsingItem(stack, world, user)
        if (user is ServerPlayerEntity) Criteria.CONSUME_ITEM.trigger(user, stack)
        if (user is PlayerEntity) user.awardStat(Stats.ITEM_USED.get(this))
        user.gameEvent(GameEvent.DRINK)
        if (!world.isClientSide) {
            if (flaming != null) user.igniteForSeconds(flaming.toFloat())
        }
        return if (stack.isEmpty) {
            Items.GLASS_BOTTLE.createItemStack()
        } else {
            if (user !is PlayerEntity || !user.abilities.instabuild) user.obtain(Items.GLASS_BOTTLE.createItemStack())
            stack
        }
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 32
    override fun getUseAnimation(stack: ItemStack) = UseAction.DRINK
    override fun use(world: Level, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> = ItemUsage.startUsingInstantly(world, user, hand)
}
