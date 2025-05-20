package miragefairy2024.util

import kotlinx.coroutines.CompletableDeferred
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.advancements.critereon.LocationPredicate
import net.minecraft.advancements.critereon.PlayerTrigger
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure

class AdvancementCard(
    private val identifier: ResourceLocation,
    private val context: Context,
    private val icon: () -> ItemStack,
    name: EnJa,
    description: EnJa,
    private val criterion: (HolderLookup.Provider) -> Pair<String, Criterion<*>>,
    private val silent: Boolean = false,
) {
    companion object {
        fun hasItem(itemGetter: () -> Item): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { _ ->
                val item = itemGetter()
                Pair("has_${item.getIdentifier().path}", InventoryChangeTrigger.TriggerInstance.hasItems(item))
            }
        }

        @JvmName("visitStructure")
        fun visit(structure: ResourceKey<Structure>): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { registries ->
                Pair(
                    "visit_${structure.location().path}",
                    PlayerTrigger.TriggerInstance.located(
                        LocationPredicate.Builder.inStructure(
                            registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(structure)
                        )
                    ),
                )
            }
        }

        @JvmName("visitBiome")
        fun visit(biome: ResourceKey<Biome>): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { registries ->
                Pair(
                    biome.location().path,
                    PlayerTrigger.TriggerInstance.located(
                        LocationPredicate.Builder.inBiome(
                            registries.lookupOrThrow(Registries.BIOME).getOrThrow(biome)
                        )
                    ),
                )
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
        DataGenerationEvents.onGenerateAdvancement { registries, writer ->
            val advancement = Advancement.Builder.advancement()
                .let { if (context is Sub) it.parent(context.parent()) else it }
                .display(
                    icon(),
                    text { nameTranslation() },
                    text { descriptionTranslation() },
                    if (context is Root) context.texture else null,
                    AdvancementType.TASK,
                    !silent,
                    !silent,
                    false
                )
                .let {
                    val pair = criterion(registries)
                    it.addCriterion(pair.first, pair.second)
                }
                .save(writer, identifier.string)
            deferred.complete(advancement)
        }
    }
}
