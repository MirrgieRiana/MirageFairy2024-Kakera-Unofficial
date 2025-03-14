package miragefairy2024.util

import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

/** @param itemStack 内部でコピーされるため、破壊されません。 */
fun Entity.obtain(itemStack: ItemStack) {
    val itemEntity = this.dropStack(itemStack.copy(), 0.5F)
    if (itemEntity != null) {
        itemEntity.resetPickupDelay()
        itemEntity.setOwner(this.uuid)
    }
}

val Entity.eyeBlockPos get() = this.eyePos.toBlockPos()

val Hand.opposite get() = if (this == Hand.MAIN_HAND) Hand.OFF_HAND else Hand.MAIN_HAND
