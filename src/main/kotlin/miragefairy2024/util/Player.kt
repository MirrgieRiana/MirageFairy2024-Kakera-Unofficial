package miragefairy2024.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

/** @param itemStack 内部でコピーされるため、破壊されません。 */
fun PlayerEntity.obtain(itemStack: ItemStack) {
    val itemEntity = this.dropStack(itemStack.copy(), 0.5F)
    if (itemEntity != null) {
        itemEntity.resetPickupDelay()
        itemEntity.setOwner(this.uuid)
    }
}
