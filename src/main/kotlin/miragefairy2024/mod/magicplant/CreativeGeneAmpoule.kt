package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.string
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.or
import net.minecraft.world.item.TooltipFlag as TooltipContext
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext as ItemUsageContext
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.network.chat.Component as Text
import net.minecraft.world.InteractionResult as ActionResult
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.world.level.Level as World

val creativeGeneAmpouleItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("creative_gene_ampoule"), "Creative Gene Ampoule", "アカーシャによる生命設計の針",
) { CreativeGeneAmpouleCard.item.createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(TraitCard.AIR_ADAPTATION.trait, 1))) } }

object CreativeGeneAmpouleCard {
    val identifier = MirageFairy2024.identifier("creative_gene_ampoule")
    val item = CreativeGeneAmpouleItem(Item.Settings().maxCount(1))
}

context(ModContext)
fun initCreativeGeneAmpoule() {
    creativeGeneAmpouleItemGroupCard.init()
    CreativeGeneAmpouleCard.let { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(creativeGeneAmpouleItemGroupCard.itemGroupKey) {
            traitRegistry.sortedEntrySet.map { (_, trait) ->
                card.item.createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1))) }
            }
        }
        card.item.registerModelGeneration(createCreativeGeneAmpouleModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            if (tintIndex == 1) {
                itemStack.getTraitStacks().or { return@registerColorProvider 0xFFFFFF }.traitStackList.firstOrNull().or { return@registerColorProvider 0xFFFFFF }.trait.primaryEffect.color
            } else {
                0xFFFFFF
            }
        }
        card.item.enJa(EnJa("Creative Gene Ampoule", "アカーシャによる生命創造の針"))
        val poemList = PoemList(99)
            .poem("This allows you to freely edit traits.", "種類に従って球根を持つ草を生えさせよ。")
            .description("description1", "Use: Grant the trait", "使用時、特性を付与")
            .description("description2", "Use while sneaking: Remove the trait", "スニーク中に使用時、特性を削除")
            .description("description3", "Use: Increases bits", "使用時、ビットを増加")
            .description("description4", "Use while sneaking: Decreases bits", "スニーク中に使用時、ビットを減少")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)
    }
}

class CreativeGeneAmpouleItem(settings: Settings) : Item(settings) {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        stack.getTraitStacks().or { return }.traitStackList.forEach { traitStack ->
            tooltip += text { traitStack.trait.getName().style(traitStack.trait.style) + " "() + traitStack.level.toString(2)() }
        }
    }

    override fun getName(stack: ItemStack): Text {
        val traitStacks = stack.getTraitStacks() ?: return super.getName(stack)
        val traitStack = traitStacks.traitStackList.firstOrNull() ?: return super.getName(stack)
        return text { traitStack.trait.getName() + " "() + traitStack.level.toString(2)() }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val blockEntity = context.world.getMagicPlantBlockEntity(context.blockPos) ?: return ActionResult.PASS
        if (context.world.isClientSide) return ActionResult.CONSUME
        val a = blockEntity.getTraitStacks() ?: TraitStacks.EMPTY
        val b = context.stack.getTraitStacks() ?: TraitStacks.EMPTY
        if (context.player?.isSneaking != true) {
            blockEntity.setTraitStacks(a + b)
        } else {
            blockEntity.setTraitStacks(a - b)
        }
        return ActionResult.CONSUME
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        if (world.isClientSide) return TypedActionResult.success(itemStack)
        val traitStacks = itemStack.getTraitStacks() ?: TraitStacks.EMPTY
        if (!user.isSneaking) {
            itemStack.setTraitStacks(TraitStacks.of(traitStacks.traitStackMap.mapValues { (it.value shl 1).let { level -> if (level <= 0) 1 else level } }))
        } else {
            itemStack.setTraitStacks(TraitStacks.of(traitStacks.traitStackMap.mapValues { (it.value shr 1).let { level -> if (level <= 0) 1 else level } }))
        }
        return TypedActionResult.consume(itemStack)
    }
}

private fun createCreativeGeneAmpouleModel() = Model {
    ModelData(
        parent = Identifier("item/generated"),
        textures = ModelTexturesData(
            "layer0" to MirageFairy2024.identifier("item/creative_gene_ampoule_casing").string,
            "layer1" to MirageFairy2024.identifier("item/creative_gene_ampoule_liquid").string,
            "layer2" to MirageFairy2024.identifier("item/creative_gene_ampoule_highlight").string,
        ),
    )
}
