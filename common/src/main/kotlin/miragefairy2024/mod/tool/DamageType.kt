package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.registerDamageTypeTagGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.toDamageTypeTag
import miragefairy2024.util.with
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.DamageTypeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType

@Suppress("LeakingThis")
abstract class DamageTypeCard {
    abstract fun getPath(): String
    val identifier = MirageFairy2024.identifier(getPath())
    val registryKey = Registries.DAMAGE_TYPE with identifier

    open val exhaustion = 0.1F
    val damageType = DamageType(identifier.toLanguageKey(), exhaustion)

    abstract fun getKillMessage(): EnJa
    abstract fun getPlayerKillMessage(): EnJa
    abstract fun getTags(): List<TagKey<DamageType>>

    context(ModContext)
    fun init() {
        registerDynamicGeneration(registryKey) {
            damageType
        }

        en { identifier.toLanguageKey("death.attack") to getKillMessage().en }
        ja { identifier.toLanguageKey("death.attack") to getKillMessage().ja }
        en { identifier.toLanguageKey("death.attack", "player") to getPlayerKillMessage().en }
        ja { identifier.toLanguageKey("death.attack", "player") to getPlayerKillMessage().ja }
        getTags().forEach {
            identifier.registerDamageTypeTagGeneration { it }
        }
    }
}

object MagicDamageTypeCard : DamageTypeCard() {
    override fun getPath() = "magic"
    override fun getKillMessage() = EnJa("%1\$s was killed by magic", "%1\$sは魔法で殺された")
    override fun getPlayerKillMessage() = EnJa("%1\$s was killed by magic whilst trying to escape %2\$s", "%1\$sは%2\$sとの戦闘中に魔法で殺された")
    override fun getTags() = listOf(DamageTypeTags.IS_PROJECTILE, DamageTypeTags.BYPASSES_ARMOR, C_IS_MAGIC_DAMAGE_TYPE_TAG, NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG)
}

object PhysicalMagicDamageTypeCard : DamageTypeCard() {
    override fun getPath() = "physical_magic"
    override fun getKillMessage() = EnJa("%1\$s was killed by magic", "%1\$sは魔法で殺された")
    override fun getPlayerKillMessage() = EnJa("%1\$s was killed by magic whilst trying to escape %2\$s", "%1\$sは%2\$sとの戦闘中に魔法で殺された")
    override fun getTags() = listOf(DamageTypeTags.IS_PROJECTILE, C_IS_MAGIC_DAMAGE_TYPE_TAG, NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG)
}

object ToolBreakDamageTypeCard : DamageTypeCard() {
    override fun getPath() = "tool_break"
    override fun getKillMessage() = EnJa("%1\$s injured their hand", "%1\$sは手を怪我した")
    override fun getPlayerKillMessage() = EnJa("%1\$s injured their hand whilst trying to escape %2\$s", "%1\$sは%2\$sとの戦闘中に手を怪我した")
    override fun getTags() = listOf(DamageTypeTags.NO_KNOCKBACK)
}

val C_IS_MAGIC_DAMAGE_TYPE_TAG = ResourceLocation.fromNamespaceAndPath("c", "is_magic").toDamageTypeTag()
val NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG = ResourceLocation.fromNamespaceAndPath("neoforge", "is_magic").toDamageTypeTag()
val IS_MAGIC_DAMAGE_TYPE_TAG = MirageFairy2024.identifier("is_magic").toDamageTypeTag()

context(ModContext)
fun initDamageType() {
    MagicDamageTypeCard.init()
    PhysicalMagicDamageTypeCard.init()
    ToolBreakDamageTypeCard.init()

    DamageTypeTags.BYPASSES_ARMOR.registerDamageTypeTagGeneration { IS_MAGIC_DAMAGE_TYPE_TAG }
    C_IS_MAGIC_DAMAGE_TYPE_TAG.registerDamageTypeTagGeneration { IS_MAGIC_DAMAGE_TYPE_TAG }
    NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG.registerDamageTypeTagGeneration { IS_MAGIC_DAMAGE_TYPE_TAG }
}
