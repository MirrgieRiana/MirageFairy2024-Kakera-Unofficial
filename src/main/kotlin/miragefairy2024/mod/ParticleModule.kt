package miragefairy2024.mod

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.register
import miragefairy2024.util.string
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.particle.DefaultParticleType
import net.minecraft.registry.Registries

enum class ParticleTypeCard(
    path: String,
    textureName: String,
    alwaysSpawn: Boolean,
) {
    MISSION("mission", "mission", true),
    COLLECTING_MAGIC("collecting_magic", "magic", false),
    DESCENDING_MAGIC("descending_magic", "magic", false),
    MIRAGE_FLOUR("mirage_flour", "mirage_flour", false),
    ATTRACTING_MAGIC("attracting_magic", "mission", false),
    AURA("aura", "mission", false),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val texture = MirageFairy2024.identifier(textureName)
    val particleType: DefaultParticleType = FabricParticleTypes.simple(alwaysSpawn)
}

context(ModContext)
fun initParticleModule() {
    ParticleTypeCard.entries.forEach { card ->
        card.particleType.register(Registries.PARTICLE_TYPE, card.identifier)
        DataGenerationEvents.onGenerateParticles {
            val data = jsonObject(
                "textures" to jsonArray(
                    card.texture.string.jsonElement,
                ),
            )
            it(card.identifier, data)
        }
    }
}
