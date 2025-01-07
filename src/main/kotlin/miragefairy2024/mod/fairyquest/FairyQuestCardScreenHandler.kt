package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.OutputSlot
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.mergeTo
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.set
import miragefairy2024.util.size
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier

val fairyQuestCardScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
    FairyQuestCardScreenHandler(syncId, playerInventory, fairyQuestRecipeRegistry.get(Identifier(buf.readString()))!!, ScreenHandlerContext.EMPTY)
}

val guiFairyQuestCardFullScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("fairy_quest_card").toTranslationKey()}.fullScreen" }, "Click to full screen", "クリックで全画面表示")

context(ModContext)
fun initFairyQuestCardScreenHandler() {
    fairyQuestCardScreenHandlerType.register(Registries.SCREEN_HANDLER, MirageFairy2024.identifier("fairy_quest_card"))
    guiFairyQuestCardFullScreenTranslation.enJa()
}

class FairyQuestCardScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val recipe: FairyQuestRecipe, val context: ScreenHandlerContext) : ScreenHandler(fairyQuestCardScreenHandlerType, syncId) {
    private val inputInventory = SimpleInventory(4)
    private var processingInventory = SimpleInventory(0)
    private var resultInventory = SimpleInventory(0)
    private val outputInventory = SimpleInventory(4)
    var progress = 0

    private val propertyDelegate = object : PropertyDelegate {
        override fun get(index: Int) = when (index) {
            0 -> progress
            else -> 0
        }

        override fun set(index: Int, value: Int) = when (index) {
            0 -> progress = value
            else -> Unit
        }

        override fun size() = 1
    }

    init {
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(playerInventory, c, 0, 0))
        }
        repeat(4) { i ->
            addSlot(object : Slot(inputInventory, i, 0, 0) {
                override fun canInsert(stack: ItemStack): Boolean {
                    val input = recipe.inputs.getOrNull(i) ?: return false
                    return input.first.test(stack)
                }
            })
        }
        repeat(4) { i ->
            addSlot(OutputSlot(outputInventory, i, 0, 0))
        }
        addProperties(propertyDelegate)
    }

    override fun canUse(player: PlayerEntity) = true

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        val playerIndices = 9 * 4 - 1 downTo 0
        val utilityIndices = 9 * 4 until 9 * 4 + 4 // TODO 出力スロットを含めると、出力スロットに既存アイテムがある場合にそこにスタックしてしまう
        val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
        return quickMove(slot, destinationIndices)
    }

    override fun sendContentUpdates() {

        // リザルトにアイテムが残っている場合、排出を試みる
        if (!resultInventory.isEmpty) {
            val result = resultInventory.mergeTo(outputInventory)
            if (!result.completed) return // リザルトにまだアイテムが残っているので次のクラフトを開始できない
        }
        // この時点でリザルトは空


        // クラフト進行処理
        if (progress == 0) { // クラフトがまだ始まっていない場合、始める

            val onCraftStart = mutableListOf<() -> Unit>()
            val processingItemStacks = mutableListOf<ItemStack>()

            // レシピ判定
            recipe.inputs.forEachIndexed { index, (ingredient, count) ->
                if (!ingredient.test(inputInventory[index])) return
                if (inputInventory[index].count < count) return
                onCraftStart += {
                    processingItemStacks += inputInventory[index].split(count)
                }
            }

            // 成立

            onCraftStart.forEach {
                it()
            }

            // 処理中アイテムの格納
            if (processingInventory.size < processingItemStacks.size) processingInventory = SimpleInventory(processingItemStacks.size)
            processingItemStacks.forEachIndexed { index, itemStack ->
                processingInventory[index] = itemStack
            }

            progress = 1

        } else { // クラフトが既に始まっている場合、プログレスを増加
            progress++
        }


        // クラフト完了処理
        if (progress >= recipe.duration) { // プログレスが満了している場合、完成処理

            // 材料の削除
            processingInventory.clear()

            // 成果物の生成
            if (resultInventory.size < recipe.outputs.size) resultInventory = SimpleInventory(recipe.outputs.size)
            recipe.outputs.forEachIndexed { index, itemStack ->
                resultInventory[index] = itemStack.copy()
            }

            // リザルト格納
            resultInventory.mergeTo(outputInventory)

            // エフェクト
            context.run { world, blockPos ->
                world.playSound(null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5F, 0.8F + 0.4F * world.random.nextFloat())
            }

            // リセット
            progress = 0

        }

        super.sendContentUpdates()

    }

    override fun onClosed(player: PlayerEntity) {
        super.onClosed(player)
        context.run { _, _ ->
            dropInventory(player, inputInventory)
            dropInventory(player, processingInventory)
            dropInventory(player, resultInventory)
            dropInventory(player, outputInventory)
        }
    }
}
