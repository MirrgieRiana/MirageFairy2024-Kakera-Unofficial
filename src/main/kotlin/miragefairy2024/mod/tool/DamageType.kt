package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.registerDamageTypeTagGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.with
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.DamageTypeTags

object MagicDamageTypeCard {
    val identifier = MirageFairy2024.identifier("magic")
    val registryKey = RegistryKeys.DAMAGE_TYPE with identifier
    val damageType = DamageType(identifier.toTranslationKey(), 0.1F)
}

context(ModContext)
fun initDamageType() {
    MagicDamageTypeCard.let { card ->
        registerDynamicGeneration(card.registryKey) {
            card.damageType
        }
        en { card.identifier.toTranslationKey("death.attack") to "%1\$s was killed by magic" }
        ja { card.identifier.toTranslationKey("death.attack") to "%1\$sは魔法で殺された" }
        en { card.identifier.toTranslationKey("death.attack", "player") to "%1\$s was killed by magic whilst trying to escape %2\$s" }
        ja { card.identifier.toTranslationKey("death.attack", "player") to "%1\$sは%2\$sとの戦闘中に魔法で殺された" }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.IS_PROJECTILE }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.BYPASSES_ARMOR }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.WITCH_RESISTANT_TO }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.AVOIDS_GUARDIAN_THORNS }
    }
}
