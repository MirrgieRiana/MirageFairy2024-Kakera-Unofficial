package miragefairy2024.util

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack

/** @param itemStack 内部でコピーされるため、破壊されません。 */
fun Entity.obtain(itemStack: ItemStack) {
    val itemEntity = this.spawnAtLocation(itemStack.copy(), 0.5F)
    if (itemEntity != null) {
        itemEntity.setNoPickUpDelay()
        itemEntity.setTarget(this.uuid)
    }
}

val Entity.eyeBlockPos get() = this.eyePosition.toBlockPos()

val InteractionHand.opposite get() = if (this == InteractionHand.MAIN_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND
