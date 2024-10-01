package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.text
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.invoke
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.with
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.castOrNull
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.data.client.TextureKey
import net.minecraft.data.client.TexturedModel
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.function.CopyNbtLootFunction
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

object FairyStatue {
    val itemGroupCard = ItemGroupCard(MirageFairy2024.identifier("fairy_statue"), "Fairy Statue", "妖精の像") {
        FairyStatueCard.FAIRY_STATUE.item.createItemStack().setFairyStatueMotif(motifRegistry.entrySet.random().value)
    }
    val descriptionTranslation = Translation({ "block.${MirageFairy2024.MOD_ID}.fairy_statue.description" }, "Fairy dream can be obtained", "妖精の夢を獲得可能")
    val CASE: TextureKey = TextureKey.of("case")
    val BASE: TextureKey = TextureKey.of("base")
    val END: TextureKey = TextureKey.of("end")
}

class FairyStatueCard(
    path: String,
    val brokenName: Pair<String, String>,
    format: Pair<String, String>,
    poem: Pair<String, String>,
    mapColor: MapColor,
) {
    val identifier = MirageFairy2024.identifier(path)
    val block = FairyStatueBlock(this, FabricBlockSettings.create().mapColor(mapColor).strength(0.5F).nonOpaque())
    val blockEntityType: BlockEntityType<FairyStatueBlockEntity> = BlockEntityType({ pos, state -> FairyStatueBlockEntity(this, pos, state) }, setOf(block), null)
    val item = FairyStatueBlockItem(this, block, Item.Settings())
    val formatTranslation = Translation({ identifier.toTranslationKey("block", "format") }, format.first, format.second)
    val poemList = PoemList(0)
        .poem(poem.first, poem.second)
        .text(PoemType.DESCRIPTION, FairyStatue.descriptionTranslation())
    val texturedModelFactory = TexturedModel.Factory {
        val model = Model(MirageFairy2024.identifier("block/fairy_statue_template"), TextureKey.PARTICLE, FairyStatue.CASE, FairyStatue.BASE, FairyStatue.END)
        model.with(
            TextureKey.PARTICLE to "block/" * identifier * "_base",
            FairyStatue.CASE to "block/" * identifier * "_case",
            FairyStatue.BASE to "block/" * identifier * "_base",
            FairyStatue.END to "block/" * identifier * "_end",
        )
    }

    companion object {
        val entries = mutableListOf<FairyStatueCard>()

        val FAIRY_STATUE = FairyStatueCard(
            "fairy_statue",
            Pair("Broken Fairy Statue", "破損した妖精の像"),
            Pair("%s Statue", "%sの像"),
            Pair("Mysterious Method of Creation", "その製法は誰にも知られていない"),
            MapColor.IRON_GRAY,
        ).also { entries += it }
        val GOLDEN_FAIRY_STATUE = FairyStatueCard(
            "golden_fairy_statue",
            Pair("Broken Fairy Statue", "破損した妖精の像"),
            Pair("%s Statue", "%sの像"),
            Pair("Was their hair always this long...?", "妖精の髪が伸びている気がする…"),
            MapColor.GOLD,
        ).also { entries += it }
        val FANTASTIC_FAIRY_STATUE = FairyStatueCard(
            "fantastic_fairy_statue",
            Pair("Broken Fairy Statue", "破損した妖精の像"),
            Pair("%s Statue", "%sの像"),
            Pair("Glossier and more beautiful.", "その翅は艶やかで、本物よりも美しい。"),
            MapColor.MAGENTA,
        ).also { entries += it }
    }
}


context(ModContext)
fun initFairyStatue() {

    FairyStatue.itemGroupCard.init()

    FairyStatue.descriptionTranslation.enJa()

    FairyStatueCard.entries.forEach { card ->

        // 登録
        card.block.register(Registries.BLOCK, card.identifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        // アイテムグループ
        card.item.registerItemGroup(FairyStatue.itemGroupCard.itemGroupKey) {
            motifRegistry.sortedEntrySet.map { card.item.createItemStack().setFairyStatueMotif(it.value) }
        }

        // レンダリング
        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        card.block.registerModelGeneration(card.texturedModelFactory)
        card.block.registerCutoutRenderLayer()
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()
        card.item.registerGeneratedModelGeneration()

        // 翻訳
        card.block.enJa(card.brokenName.first, card.brokenName.second)
        card.formatTranslation.enJa()
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        // タグ
        card.block.registerBlockTagGeneration { BlockTags.PICKAXE_MINEABLE }

        // ドロップ
        card.block.registerLootTableGeneration { provider ->
            LootTable(
                LootPool(ItemLootPoolEntry(card.item)) {
                    rolls(ConstantLootNumberProvider.create(1.0F))
                    apply(CopyNbtLootFunction.builder(ContextLootNbtProvider.BLOCK_ENTITY).withOperation("Motif", "Motif"))
                    provider.addSurvivesExplosionCondition(card.item, this)
                },
            )
        }

    }

}


class FairyStatueBlock(private val card: FairyStatueCard, settings: Settings) : SimpleHorizontalFacingBlock(settings), BlockEntityProvider, FairyDreamProviderBlock {
    companion object {
        private val SHAPE: VoxelShape = createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = FairyStatueBlockEntity(card, pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world.isClient) return
        val blockEntity = world.getBlockEntity(pos) as? FairyStatueBlockEntity ?: return
        blockEntity.setMotif(itemStack.getFairyStatueMotif())
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType?) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = SHAPE

    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        return asItem().createItemStack().setFairyStatueMotif(world.getBlockEntity(pos).castOrNull<FairyStatueBlockEntity>()?.getMotif())
    }

    override fun getFairyDreamMotifs(world: World, blockPos: BlockPos): List<Motif> {
        val blockEntity = world.getBlockEntity(blockPos) as? FairyStatueBlockEntity ?: return listOf()
        return blockEntity.getMotif()?.let { listOf(it) } ?: listOf()
    }

}

class FairyStatueBlockEntity(card: FairyStatueCard, pos: BlockPos, state: BlockState) : BlockEntity(card.blockEntityType, pos, state), RenderingProxyBlockEntity {
    companion object {
        private val INVALID_ITEM_STACK = EMPTY_ITEM_STACK
    }

    private var motif: Motif? = null
    private var itemStackCache: ItemStack? = null

    fun getMotif() = motif

    fun setMotif(motif: Motif?) {
        this.motif = motif
        itemStackCache = motif?.createFairyItemStack()
        markDirty()
    }


    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["Motif"].string.set(getMotif()?.getIdentifier()?.string)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        setMotif(nbt.wrapper["Motif"].string.get()?.toIdentifier()?.toFairyMotif())
    }

    override fun toInitialChunkDataNbt(): NbtCompound = createNbt()
    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)


    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        renderingProxy.stack {
            renderingProxy.translate(8.0 / 16.0, 5.5 / 16.0, 8.0 / 16.0)
            renderingProxy.rotateY(-(cachedState[HorizontalFacingBlock.FACING].horizontal * 90) / 180F * Math.PI.toFloat())
            renderingProxy.renderItemStack(itemStackCache ?: INVALID_ITEM_STACK)
        }
    }

}


class FairyStatueBlockItem(private val card: FairyStatueCard, block: Block, settings: Settings) : BlockItem(block, settings), FairyDreamProviderItem {

    override fun getName(stack: ItemStack) = stack.getFairyStatueMotif()?.let { card.formatTranslation(it.displayName) } ?: super.getName(stack).red

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val motif = stack.getFairyStatueMotif() ?: return
        val fairyItemStack = motif.createFairyItemStack()

        tooltip += text { empty() }
        fairyItemStack.item.appendTooltip(fairyItemStack, world, tooltip, context)
    }

    override fun getFairyDreamMotifs(itemStack: ItemStack) = itemStack.getFairyStatueMotif()?.let { listOf(it) } ?: listOf()

}

fun ItemStack.getFairyStatueMotifId(): Identifier? = this.nbt.or { return null }.wrapper["Motif"].string.get().or { return null }.toIdentifier()
fun ItemStack.setFairyStatueMotifId(fairyStatueMotifId: Identifier?) = this.also { this.getOrCreateNbt().wrapper["Motif"].string.set(fairyStatueMotifId?.string) }

fun ItemStack.getFairyStatueMotif() = this.getFairyStatueMotifId()?.toFairyMotif()
fun ItemStack.setFairyStatueMotif(fairyStatueMotif: Motif?) = this.setFairyStatueMotifId(fairyStatueMotif?.getIdentifier())
