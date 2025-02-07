package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text

enum class MainHandConditionCard(path: String, en: String, ja: String, val predicate: (ItemStack) -> Boolean) {
    HOE("hoe", "Hoe", "クワ", { it.isIn(ItemTags.HOES) }),
    SWORD("sword", "Sword", "剣", { it.isIn(ItemTags.SWORDS) }),
    ;

    val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_condition.main_hand.$path" }, en, ja)
}

class MainHandPassiveSkillCondition(private val card: MainHandConditionCard) : PassiveSkillCondition {
    override val text: Text get() = text { card.translation() }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = card.predicate(context.player.mainHandStack)
}
