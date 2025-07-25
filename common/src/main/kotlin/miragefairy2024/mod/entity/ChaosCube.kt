package miragefairy2024.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.SoundEventCard
import miragefairy2024.mod.SoundEventChannel
import miragefairy2024.mod.SoundEventPacket
import miragefairy2024.mod.materials.item.MaterialCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.particle.MagicSquareParticleChannel
import miragefairy2024.mod.particle.MagicSquareParticlePacket
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.structure.DripstoneCavesRuinCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
import miragefairy2024.util.Registration
import miragefairy2024.util.configure
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerEntityTypeTagGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSpawn
import miragefairy2024.util.sendToAround
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.EntityTypeTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.SpawnPlacementTypes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition
import org.joml.Quaternionf
import java.util.EnumSet
import kotlin.math.sqrt
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.util.Mth as MathHelper
import net.minecraft.world.entity.Mob as MobEntity
import net.minecraft.world.entity.MobCategory as SpawnGroup
import net.minecraft.world.entity.SpawnPlacements as SpawnRestriction
import net.minecraft.world.entity.ai.attributes.Attributes as EntityAttributes
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal as GoToWalkTargetGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal as WanderAroundFarGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal as RevengeGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal as ActiveTargetGoal
import net.minecraft.world.entity.monster.Monster as HostileEntity
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.level.biome.Biomes as BiomeKeys
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction as SetCountLootFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition as KilledByPlayerLootCondition
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator as UniformLootNumberProvider
import net.minecraft.world.phys.Vec3 as Vec3d

object ChaosCubeCard {
    val spawnGroup = SpawnGroup.MONSTER
    val width = 0.8F
    val height = 1.5F
    fun createEntity(entityType: EntityType<ChaosCubeEntity>, world: Level) = ChaosCubeEntity(entityType, world)
    val identifier = MirageFairy2024.identifier("chaos_cube")
    val name = EnJa("Chaos Cube", "混沌のキューブ")
    val entityType = Registration(BuiltInRegistries.ENTITY_TYPE, identifier) {
        FabricEntityTypeBuilder.create(spawnGroup) { entityType, world -> createEntity(entityType, world) }
            .dimensions(EntityDimensions.fixed(width, height))
            .build()
    }
    val spawnEggItem = Registration(BuiltInRegistries.ITEM, identifier * "_egg") { SpawnEggItem(entityType.await(), 0xB36235, 0xFFC21D, Item.Properties()) }

    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DripstoneCavesRuinCard.advancement.await() },
        icon = { MaterialCard.ETHEROBALLISTIC_BOLT_FRAGMENT.item().createItemStack() },
        name = EnJa("Magical Control Program", "魔導制御プログラム"),
        description = EnJa("Defeat the Chaos Cube that appears in the Dripstone Cave Ruin", "鍾乳洞の遺跡に出現する混沌のキューブを倒す"),
        criterion = AdvancementCard.kill(entityType),
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    fun init() {
        entityType.register()
        ModEvents.onInitialize {
            val attributes = HostileEntity.createMonsterAttributes()
                .add(EntityAttributes.MAX_HEALTH, 100.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(EntityAttributes.ARMOR, 12.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.1)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0)
            FabricDefaultAttributeRegistry.register(entityType(), attributes)
        }
        entityType.enJa(name)
        entityType.registerEntityTypeTagGeneration { EntityTypeTags.FALL_DAMAGE_IMMUNE }
        entityType.registerLootTableGeneration { registries ->
            LootTable(
                LootPool(ItemLootPoolEntry(MaterialCard.MIRAGIDIAN_SHARD.item())).configure {
                    apply(SetCountLootFunction.setCount(UniformLootNumberProvider.between(0.0F, 2.0F)))
                    apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformLootNumberProvider.between(0.0F, 1.0F)))
                },
                LootPool(ItemLootPoolEntry(MaterialCard.MIRAGIDIAN.item())).configure {
                    `when`(KilledByPlayerLootCondition.killedByPlayer())
                    `when`(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, 0.05F, 0.02F))
                },
                LootPool(ItemLootPoolEntry(MaterialCard.CHAOS_STONE.item())).configure {
                    `when`(KilledByPlayerLootCondition.killedByPlayer())
                    `when`(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, 0.3F, 0.1F))
                },
            )
        }

        entityType.registerSpawn(SpawnGroup.MONSTER, 2, 2, 4) { +BiomeKeys.DRIPSTONE_CAVES }
        ModEvents.onInitialize {
            SpawnRestriction.register(entityType(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HostileEntity::checkMonsterSpawnRules)
        }

        spawnEggItem.register()
        spawnEggItem.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        spawnEggItem.registerModelGeneration(Model(ResourceLocation.fromNamespaceAndPath("minecraft", "item/template_spawn_egg")))
        spawnEggItem.enJa(EnJa("${name.en} Spawn Egg", "${name.ja}のスポーンエッグ"))

        advancement.init()
    }
}

class ChaosCubeEntity(entityType: EntityType<out ChaosCubeEntity>, world: Level) : HostileEntity(entityType, world) {

    class Segment(val partIndex: Int) {
        val prevRotation = Quaternionf()
        val rotation = Quaternionf()
    }

    private var isFirstClientTick = true
    val segments = (0 until 8).map { Segment(it) }.toTypedArray()

    private val animator = sequence {

        while (true) {
            fun swap(a: Int, b: Int, c: Int, d: Int) {
                val tmp1 = segments[a]
                segments[a] = segments[b]
                segments[b] = segments[c]
                segments[c] = segments[d]
                segments[d] = tmp1
            }

            val duration = 40

            // ++++ ----    vv   vv       <==>
            // 0123 4567  0132 4576  0132 6754
            // 0145 2367  0154 2376  0154 6732
            // 0246 1357  0264 1375  0264 5731

            repeat(duration) {
                segments[0].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 1, 3, 2)
            swap(6, 7, 5, 4)

            repeat(duration) {
                segments[0].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 1, 5, 4)
            swap(6, 7, 3, 2)

            repeat(duration) {
                segments[0].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 2, 6, 4)
            swap(5, 7, 3, 1)

        }

    }.iterator()


    init {
        xpReward = 20
    }

    override fun registerGoals() {
        goalSelector.addGoal(4, ShootGoal(this))
        goalSelector.addGoal(5, GoToWalkTargetGoal(this, 1.0))
        goalSelector.addGoal(7, WanderAroundFarGoal(this, 0.5, 0.0F))
        targetSelector.addGoal(1, RevengeGoal(this, ChaosCubeEntity::class.java).setAlertOthers())
        targetSelector.addGoal(2, TargetGoal(this, PlayerEntity::class.java))
    }


    override fun getAmbientSound() = SoundEventCard.ENTITY_CHAOS_CUBE_AMBIENT.soundEvent
    override fun getHurtSound(source: DamageSource) = SoundEventCard.ENTITY_CHAOS_CUBE_HURT.soundEvent
    override fun getDeathSound() = SoundEventCard.ENTITY_CHAOS_CUBE_DEATH.soundEvent

    override fun aiStep() {
        super.aiStep()

        if (!onGround() && deltaMovement.y < 0.0) deltaMovement = deltaMovement.multiply(1.0, 0.6, 1.0)

        if (level().isClientSide) {

            if (isFirstClientTick) {
                isFirstClientTick = false
                animator.next()
                segments.forEach {
                    it.prevRotation.x = it.rotation.x
                    it.prevRotation.y = it.rotation.y
                    it.prevRotation.z = it.rotation.z
                    it.prevRotation.w = it.rotation.w
                }
            } else {
                segments.forEach {
                    it.prevRotation.x = it.rotation.x
                    it.prevRotation.y = it.rotation.y
                    it.prevRotation.z = it.rotation.z
                    it.prevRotation.w = it.rotation.w
                }
                animator.next()
            }

            if (level().random.nextInt(10) == 0) {
                level().addParticle(
                    ParticleTypeCard.CHAOS_STONE.particleType,
                    x + bbWidth / 2.0 * (2.0 * random.nextDouble() - 1.0) * 0.8,
                    y + 1.0,
                    z + bbWidth / 2.0 * (2.0 * random.nextDouble() - 1.0) * 0.8,
                    0.0,
                    -0.05,
                    0.0,
                )
            }

        }

    }

    private class ShootGoal(private val entity: ChaosCubeEntity) : Goal() {
        init {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun requiresUpdateEveryTick() = true

        override fun canUse(): Boolean {
            val livingEntity = entity.target
            return livingEntity != null && livingEntity.isAlive && entity.canAttack(livingEntity)
        }

        override fun canContinueToUse() = ticker != null && super.canContinueToUse()

        private var ticker: Iterator<Unit>? = null

        override fun start() {
            ticker = sequence {

                suspend fun SequenceScope<Unit>.tryWait(): Boolean {
                    repeat(entity.random.nextIntBetweenInclusive(20 * 2, 20 * 8)) {
                        yield(Unit)
                    }
                    return true
                }

                suspend fun SequenceScope<Unit>.tryShoot(): Boolean {


                    // 準備フェーズ

                    val target = (entity.target ?: return false)
                    if (!entity.sensing.hasLineOfSight(target)) return true

                    val shootingX = entity.x + entity.random.triangle(0.0, 2.0)
                    val shootingY = entity.y + entity.random.triangle(2.0, 2.0)
                    val shootingZ = entity.z + entity.random.triangle(0.0, 2.0)

                    // ターゲットを見る
                    entity.getLookControl().setLookAt(target, 10.0F, 10.0F)

                    // エフェクト
                    if (!entity.isSilent) {
                        val soundEventPacket = SoundEventPacket(
                            SoundEventCard.ENTITY_CHAOS_CUBE_ATTACK.soundEvent,
                            entity.blockPosition(),
                            SoundCategory.HOSTILE,
                            2.0F,
                            (entity.random.nextFloat() - entity.random.nextFloat()) * 0.2F + 1.0F,
                            false,
                        )
                        SoundEventChannel.sendToAround(entity.level() as ServerWorld, entity.eyePosition, 64.0, soundEventPacket)
                    }
                    val particlePacket = MagicSquareParticlePacket(
                        Vec3d(shootingX, shootingY, shootingZ),
                        Vec3d(target.x, target.getY(0.5), target.z),
                    )
                    MagicSquareParticleChannel.sendToAround(entity.level() as ServerWorld, entity.position(), 64.0, particlePacket)


                    repeat(40) {
                        yield(Unit)
                    }

                    repeat(5) { i ->

                        if (i > 0) {
                            repeat(4) {
                                yield(Unit)
                            }
                        }


                        // 射撃フェーズ

                        if (entity.target != target) return false
                        if (!entity.sensing.hasLineOfSight(target)) return true

                        val diffX = target.x - shootingX
                        val diffY = target.getY(0.5) - shootingY
                        val diffZ = target.z - shootingZ
                        val distance = sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ)
                        if (distance < 0.01) return@repeat // 近すぎるので射撃に失敗

                        // 発射体の生成
                        val projectileEntity = EtheroballisticBoltEntity(EtheroballisticBoltCard.entityType(), entity.level())
                        projectileEntity.owner = entity
                        projectileEntity.setPos(shootingX, shootingY, shootingZ)
                        projectileEntity.setDeltaMovement(
                            0.8 * entity.getRandom().triangle(diffX / distance, 0.05),
                            0.8 * entity.getRandom().triangle(diffY / distance, 0.05),
                            0.8 * entity.getRandom().triangle(diffZ / distance, 0.05),
                        )
                        projectileEntity.damage = 20.0F
                        projectileEntity.maxDistance = 32.0F
                        entity.level().addFreshEntity(projectileEntity)

                        // ターゲットを見る
                        entity.getLookControl().setLookAt(target, 10.0F, 10.0F)

                        // エフェクト
                        if (!entity.isSilent) {
                            val soundEventPacket = SoundEventPacket(
                                SoundEventCard.ENTITY_ETHEROBALLISTIC_BOLT_SHOOT.soundEvent,
                                entity.blockPosition(),
                                SoundCategory.HOSTILE,
                                2.0F,
                                (entity.random.nextFloat() - entity.random.nextFloat()) * 0.2F + 1.0F,
                                false,
                            )
                            SoundEventChannel.sendToAround(entity.level() as ServerWorld, entity.eyePosition, 64.0, soundEventPacket)
                        }


                    }

                    return true
                }

                suspend fun SequenceScope<Unit>.tryMove(): Boolean {
                    while (true) {
                        val target = entity.target ?: return false
                        if (entity.sensing.hasLineOfSight(target)) return true
                        entity.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.0)
                        yield(Unit)
                    }
                }

                while (true) {

                    if (!tryWait()) {
                        ticker = null
                        return@sequence
                    }

                    if (!tryMove()) {
                        ticker = null
                        return@sequence
                    }

                    if (!tryShoot()) {
                        ticker = null
                        return@sequence
                    }

                }

            }.iterator()
        }

        override fun stop() {
            ticker = null
        }

        override fun tick() {
            val ticker = ticker ?: return
            if (ticker.hasNext()) ticker.next()
        }

    }

    private class TargetGoal<T : LivingEntity>(mob: MobEntity, targetClass: Class<T>) : ActiveTargetGoal<T>(mob, targetClass, true) {
        override fun canUse(): Boolean {
            val world = mob.level()
            if (world.gameTime % 20L != 0L) return false
            if (world !is ServerWorld) return false
            val structure = world.structureManager().registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceKey.create(Registries.STRUCTURE, MirageFairy2024.identifier("dripstone_caves_ruin"))) // TODO
            if (!world.structureManager().getStructureAt(mob.blockPosition(), structure).isValid) return false
            return super.canUse()
        }
    }
}
