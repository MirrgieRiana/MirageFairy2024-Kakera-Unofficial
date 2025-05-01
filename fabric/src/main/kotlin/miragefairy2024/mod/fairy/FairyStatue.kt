package miragefairy2024.mod.fairy

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
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
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.empty
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.invoke
import miragefairy2024.util.normal
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
import miragefairy2024.util.withHorizontalRotation
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.castOrNull
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.network.protocol.game.ClientGamePacketListener as ClientPlayPacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket as BlockEntityUpdateS2CPacket
import net.minecraft.world.level.BlockGetter as BlockView
import net.minecraft.world.level.block.EntityBlock as BlockEntityProvider
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue as ConstantLootNumberProvider
import net.minecraft.world.phys.shapes.CollisionContext as ShapeContext

object FairyStatue {
    val itemGroupCard = ItemGroupCard(MirageFairy2024.identifier("fairy_statue"), "Fairy Statue", "妖精の像") {
        FairyStatueCard.FAIRY_STATUE.item.createItemStack().also { it.setFairyMotif(motifRegistry.entrySet().random().value) }
    }
    val descriptionTranslation = Translation({ "block.${MirageFairy2024.identifier("fairy_statue").toLanguageKey()}.description" }, "Fairy dream can be obtained", "妖精の夢を獲得可能")
    val CASE: TextureKey = TextureKey.create("case")
    val BASE: TextureKey = TextureKey.create("base")
    val END: TextureKey = TextureKey.create("end")
}

class FairyStatueCard(
    path: String,
    val brokenName: EnJa,
    format: EnJa,
    poem: EnJa,
    mapColor: MapColor,
) {
    val identifier = MirageFairy2024.identifier(path)
    val block = FairyStatueBlock(this, FabricBlockSettings.create().mapColor(mapColor).strength(0.5F).nonOpaque())
    val blockEntityType: BlockEntityType<FairyStatueBlockEntity> = BlockEntityType({ pos, state -> FairyStatueBlockEntity(this, pos, state) }, setOf(block), null)
    val item = FairyStatueBlockItem(this, block, Item.Properties())
    val formatTranslation = Translation({ identifier.toLanguageKey("block", "format") }, format)
    val poemList = PoemList(0)
        .poem(poem)
        .text(PoemType.DESCRIPTION, text { FairyStatue.descriptionTranslation() })
    val texturedModelFactory = TexturedModel.Provider {
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
            EnJa("Broken Fairy Statue", "破損した妖精の像"),
            EnJa("%s Statue", "%sの像"),
            EnJa("Mysterious Method of Creation", "その製法は誰にも知られていない"),
            MapColor.METAL,
        ).also { entries += it }
        val GOLDEN_FAIRY_STATUE = FairyStatueCard(
            "golden_fairy_statue",
            EnJa("Broken Fairy Statue", "破損した妖精の像"),
            EnJa("%s Statue", "%sの像"),
            EnJa("Was their hair always this long...?", "妖精の髪が伸びている気がする…"),
            MapColor.GOLD,
        ).also { entries += it }
        val FANTASTIC_FAIRY_STATUE = FairyStatueCard(
            "fantastic_fairy_statue",
            EnJa("Broken Fairy Statue", "破損した妖精の像"),
            EnJa("%s Statue", "%sの像"),
            EnJa("Glossier and more beautiful.", "その翅は艶やかで、本物よりも美しい。"),
            MapColor.COLOR_MAGENTA,
        ).also { entries += it }
    }
}


context(ModContext)
fun initFairyStatue() {

    FairyStatue.itemGroupCard.init()

    FairyStatue.descriptionTranslation.enJa()

    FairyStatueBlock.CODEC.register(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_statue"))

    FairyStatueCard.entries.forEach { card ->

        // 登録
        card.block.register(BuiltInRegistries.BLOCK, card.identifier)
        card.blockEntityType.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, card.identifier)
        card.item.register(BuiltInRegistries.ITEM, card.identifier)

        // アイテムグループ
        card.item.registerItemGroup(FairyStatue.itemGroupCard.itemGroupKey) {
            motifRegistry.sortedEntrySet.map { card.item.createItemStack().also { itemStack -> itemStack.setFairyMotif(it.value) } }
        }

        // レンダリング
        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        card.block.registerModelGeneration(card.texturedModelFactory)
        card.block.registerCutoutRenderLayer()
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()
        card.item.registerGeneratedModelGeneration()

        // 翻訳
        card.block.enJa(card.brokenName)
        card.formatTranslation.enJa()
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        // タグ
        card.block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_PICKAXE }

        // ドロップ
        card.block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(card.item)) {
                    setRolls(ConstantLootNumberProvider.exactly(1.0F))
                    apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(FAIRY_MOTIF_DATA_COMPONENT_TYPE))
                    provider.applyExplosionCondition(card.item, this)
                },
            )
        }

    }

}


class FairyStatueBlock(private val card: FairyStatueCard, settings: Properties) : SimpleHorizontalFacingBlock(settings), BlockEntityProvider, FairyDreamProviderBlock {
    companion object {
        val CODEC: MapCodec<FairyStatueBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.xmap(
                    { identifier -> FairyStatueCard.entries.first { it.identifier == identifier } },
                    { card -> card.identifier },
                ).fieldOf("type").forGetter { it.card },
                propertiesCodec(),
            ).apply(instance, ::FairyStatueBlock)
        }
        private val SHAPE: VoxelShape = box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0)
    }

    override fun codec() = CODEC

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = FairyStatueBlockEntity(card, pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun triggerEvent(state: BlockState, world: Level, pos: BlockPos, type: Int, data: Int): Boolean {
        super.triggerEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.triggerEvent(type, data)
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack)
        if (world.isClientSide) return
        val blockEntity = world.getBlockEntity(pos) as? FairyStatueBlockEntity ?: return
        blockEntity.setMotif(itemStack.getFairyMotif())
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = SHAPE

    override fun getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        return asItem().createItemStack().also { itemStack -> itemStack.setFairyMotif(world.getBlockEntity(pos).castOrNull<FairyStatueBlockEntity>()?.getMotif()) }
    }

    override fun getFairyDreamMotifs(world: Level, blockPos: BlockPos): List<Motif> {
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
        setChanged()
    }


    override fun saveAdditional(nbt: NbtCompound, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        nbt.wrapper["Motif"].string.set(getMotif()?.getIdentifier()?.string)
    }

    override fun loadAdditional(nbt: NbtCompound, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        setMotif(nbt.wrapper["Motif"].string.get()?.toIdentifier()?.toFairyMotif())
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): NbtCompound = saveWithoutMetadata(registries)
    override fun getUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)


    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        renderingProxy.stack {
            renderingProxy.translate(8.0 / 16.0, 5.5 / 16.0, 8.0 / 16.0)
            renderingProxy.rotateY(-(blockState.getValue(HorizontalFacingBlock.FACING).get2DDataValue() * 90) / 180F * Math.PI.toFloat())
            renderingProxy.renderItemStack(itemStackCache ?: INVALID_ITEM_STACK)
        }
    }

}


class FairyStatueBlockItem(private val card: FairyStatueCard, block: Block, settings: Properties) : BlockItem(block, settings), FairyDreamProviderItem {

    override fun getName(stack: ItemStack) = stack.getFairyMotif()?.let { text { card.formatTranslation(it.displayName) } } ?: super.getName(stack).red

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        val motif = stack.getFairyMotif() ?: return
        val fairyItemStack = motif.createFairyItemStack()

        tooltipComponents += text { empty() }
        fairyItemStack.item.appendHoverText(fairyItemStack, context, tooltipComponents, tooltipFlag)
    }

    override fun getFairyDreamMotifs(itemStack: ItemStack) = itemStack.getFairyMotif()?.let { listOf(it) } ?: listOf()

}
