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
import net.minecraft.util.Identifier

enum class ParticleTypeCard(
    path: String,
    textureNames: List<String>,
    alwaysSpawn: Boolean,
) {
    MISSION("mission", listOf("mission"), true),
    COLLECTING_MAGIC("collecting_magic", listOf("magic"), false),
    DESCENDING_MAGIC("descending_magic", listOf("magic"), false),
    MIRAGE_FLOUR("mirage_flour", listOf("mirage_flour"), false),
    ATTRACTING_MAGIC("attracting_magic", listOf("mission"), false),
    AURA("aura", listOf("mission"), false),
    CHAOS_STONE("chaos_stone", listOf("chaos_stone"), false),
    HAIMEVISKA_BLOSSOM("haimeviska_blossom", listOf("haimeviska_blossom"), false),
    DRIPPING_HAIMEVISKA_SAP("dripping_haimeviska_sap", listOf("minecraft:drip_hang"), false),
    FALLING_HAIMEVISKA_SAP("falling_haimeviska_sap", listOf("minecraft:drip_fall"), false),
    LANDING_HAIMEVISKA_SAP("landing_haimeviska_sap", listOf("minecraft:drip_land"), false),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val textures = textureNames.map { if (":" in it) Identifier(it) else MirageFairy2024.identifier(it) }
    val particleType: DefaultParticleType = FabricParticleTypes.simple(alwaysSpawn)
}

context(ModContext)
fun initParticleModule() {
    ParticleTypeCard.entries.forEach { card ->
        card.particleType.register(Registries.PARTICLE_TYPE, card.identifier)
        DataGenerationEvents.onGenerateParticles {
            val data = jsonObject(
                "textures" to card.textures.map { it.string.jsonElement }.jsonArray,
            )
            it(card.identifier, data)
        }
    }
}
