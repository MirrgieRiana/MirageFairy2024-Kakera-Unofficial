package miragefairy2024.fabric

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.PlatformProxy
import miragefairy2024.fabric.mixins.api.ItemEnchantmentsCallback
import net.minecraft.core.registries.Registries

class FabricPlatformProxy : PlatformProxy {
    override fun registerModifyItemEnchantmentsHandler(handler: ModifyItemEnchantmentsHandler) {
        ItemEnchantmentsCallback.EVENT.register { itemStack, mutableItemEnchantments ->
            val server = currentServer ?: return@register
            handler.modifyItemEnchantments(itemStack, mutableItemEnchantments, server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT))
        }
    }
}
