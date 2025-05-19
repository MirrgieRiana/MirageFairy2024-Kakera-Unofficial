package miragefairy2024.util

import kotlinx.coroutines.CompletableDeferred
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

class AdvancementCard(
    val identifier: ResourceLocation,
    private val icon: Item,
    name: EnJa,
    description: EnJa,
) {
    private val deferred = CompletableDeferred<AdvancementHolder>()
    suspend fun await() = deferred.await()

    private val nameTranslation = Translation({ identifier.toLanguageKey("advancements", "title").replace('/', '.') }, name)
    private val descriptionTranslation = Translation({ identifier.toLanguageKey("advancements", "description").replace('/', '.') }, description)

    context(ModContext)
    fun init() {
        nameTranslation.enJa()
        descriptionTranslation.enJa()
        DataGenerationEvents.onGenerateAdvancement {
            val advancement = Advancement.Builder.advancement()
                .parent(MaterialCard.MIRAGE_FLOUR.advancementCard!!.await())
                .display(
                    icon,
                    text { nameTranslation() },
                    text { descriptionTranslation() },
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("has_fairy_crystal", InventoryChangeTrigger.TriggerInstance.hasItems(item))
                .save(it, identifier.string)
            deferred.complete(advancement)
        }
    }
}
