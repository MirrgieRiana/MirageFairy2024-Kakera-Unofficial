package miragefairy2024.client.util

import miragefairy2024.ModContext
import miragefairy2024.mixin.client.api.inputEventsHandlers
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping

/** @param category この文字列は識別と翻訳キーの両方に使われます。 */
class KeyMappingCard(
    private val nameTranslationKey: String,
    private val defaultKeyCode: Int,
    private val category: String,
    private val listener: () -> Unit,
) {
    lateinit var keyMapping: KeyMapping

    context(ModContext)
    fun init() {
        keyMapping = KeyMapping(nameTranslationKey, defaultKeyCode, category)
        inputEventsHandlers += {
            while (keyMapping.consumeClick()) {
                listener()
            }
        }
        KeyBindingHelper.registerKeyBinding(keyMapping)
    }
}
