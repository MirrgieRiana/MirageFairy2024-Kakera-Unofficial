package miragefairy2024.util

import me.shedaniel.math.Point
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.item.ItemStack


fun ItemStack.toEntryStack(): EntryStack<ItemStack> = EntryStacks.of(this)

fun EntryStack<*>.toEntryIngredient(): EntryIngredient = EntryIngredient.of(this)
fun Iterable<EntryStack<*>>.toEntryIngredient(): EntryIngredient = EntryIngredient.of(this)


operator fun Point.plus(other: Point) = Point(this.x + other.x, this.y + other.y)
operator fun Point.minus(other: Point) = Point(this.x - other.x, this.y - other.y)
