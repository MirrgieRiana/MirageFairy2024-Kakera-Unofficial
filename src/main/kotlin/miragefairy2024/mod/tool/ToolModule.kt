package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemModelGeneration
import net.minecraft.data.client.Models
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

fun initToolModule() {
    ToolCard.entries.forEach {
        it.init()
    }
}


interface ToolType<I : Item> {
    fun createItem(): I
    fun init(card: ToolCard<I>) = Unit
}

class ToolCard<I : Item>(
    path: String,
    private val enName: String,
    private val jaName: String,
    private val enPoem: String,
    private val jaPoem: String,
    private val tier: Int,
    private val type: ToolType<I>,
    private val initializer: ToolCard<I>.() -> Unit = {},
) {
    val identifier = Identifier(MirageFairy2024.modId, path)
    val item = type.createItem()

    fun init() {
        item.register(Registries.ITEM, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        item.registerItemModelGeneration(Models.HANDHELD)

        item.enJa(enName, jaName)

        val poemList = PoemList(tier).poem(enPoem, jaPoem)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        type.init(this)
        initializer(this)
    }

    @Suppress("unused")
    companion object {
        val entries = mutableListOf<ToolCard<*>>()
        private fun <I : Item> ToolCard<I>.register() = this.also { entries.add(this) }
    }
}
