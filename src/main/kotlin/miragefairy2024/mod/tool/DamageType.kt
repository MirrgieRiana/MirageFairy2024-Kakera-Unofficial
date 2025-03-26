package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.registerDamageTypeTagGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.with
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.registry.tag.TagKey

@Suppress("LeakingThis")
abstract class DamageTypeCard {
    abstract val path: String
    val identifier = MirageFairy2024.identifier(path)
    val registryKey = RegistryKeys.DAMAGE_TYPE with identifier

    open val exhaustion = 0.1F
    val damageType = DamageType(identifier.toTranslationKey(), exhaustion)

    abstract val killMessage: EnJa
    abstract val playerKillMessage: EnJa
    abstract val tags: List<TagKey<DamageType>>

    context(ModContext)
    fun init() {
        registerDynamicGeneration(MagicDamageTypeCard.registryKey) {
            MagicDamageTypeCard.damageType
        }

        en { MagicDamageTypeCard.identifier.toTranslationKey("death.attack") to MagicDamageTypeCard.killMessage.en }
        ja { MagicDamageTypeCard.identifier.toTranslationKey("death.attack") to MagicDamageTypeCard.killMessage.ja }
        en { MagicDamageTypeCard.identifier.toTranslationKey("death.attack", "player") to MagicDamageTypeCard.playerKillMessage.en }
        ja { MagicDamageTypeCard.identifier.toTranslationKey("death.attack", "player") to MagicDamageTypeCard.playerKillMessage.ja }
        MagicDamageTypeCard.tags.forEach {
            MagicDamageTypeCard.identifier.registerDamageTypeTagGeneration { it }
        }
    }
}

object MagicDamageTypeCard : DamageTypeCard() {
    override val path = "magic"
    override val killMessage = EnJa("%1\$s was killed by magic", "%1\$sは魔法で殺された")
    override val playerKillMessage = EnJa("%1\$s was killed by magic whilst trying to escape %2\$s", "%1\$sは%2\$sとの戦闘中に魔法で殺された")
    override val tags = listOf(DamageTypeTags.IS_PROJECTILE, DamageTypeTags.BYPASSES_ARMOR)
}

object PhysicalMagicDamageTypeCard : DamageTypeCard() {
    override val path = "physical_magic"
    override val killMessage = EnJa("%1\$s was killed by magic", "%1\$sは魔法で殺された")
    override val playerKillMessage = EnJa("%1\$s was killed by magic whilst trying to escape %2\$s", "%1\$sは%2\$sとの戦闘中に魔法で殺された")
    override val tags = listOf(DamageTypeTags.IS_PROJECTILE)
}

context(ModContext)
fun initDamageType() {
    MagicDamageTypeCard.init()
    PhysicalMagicDamageTypeCard.init()
}
