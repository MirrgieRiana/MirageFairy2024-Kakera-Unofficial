package miragefairy2024.util

import kotlinx.coroutines.CompletableDeferred
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

class AdvancementCard(
    private val identifier: ResourceLocation,
    private val context: Context,
    private val icon: () -> Item,
    name: EnJa,
    description: EnJa,
    private val criterion: () -> Pair<String, Criterion<*>>,
) {
    companion object {
        fun item(itemGetter: () -> Item): () -> Pair<String, Criterion<*>> {
            return {
                val item = itemGetter()
                Pair("has_${item.getIdentifier().path}", InventoryChangeTrigger.TriggerInstance.hasItems(item))
            }
        }
    }

    sealed class Context
    class Root(val texture: ResourceLocation) : Context()
    class Sub(val parent: suspend () -> AdvancementHolder) : Context()

    private val nameTranslation = Translation({ identifier.toLanguageKey("advancements", "title").replace('/', '.') }, name)
    private val descriptionTranslation = Translation({ identifier.toLanguageKey("advancements", "description").replace('/', '.') }, description)

    private val deferred = CompletableDeferred<AdvancementHolder>()
    suspend fun await() = deferred.await()

    context(ModContext)
    fun init() {
        nameTranslation.enJa()
        descriptionTranslation.enJa()
        DataGenerationEvents.onGenerateAdvancement { writer ->
            val advancement = Advancement.Builder.advancement()
                .let { if (context is Sub) it.parent(context.parent()) else it }
                .display(
                    icon(),
                    text { nameTranslation() },
                    text { descriptionTranslation() },
                    if (context is Root) context.texture else null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .let {
                    val pair = criterion()
                    it.addCriterion(pair.first, pair.second)
                }
                .save(writer, identifier.string)
            deferred.complete(advancement)
        }
    }
}
