package com.bobroflza.tieredarcana.spell;

import com.bobroflza.tieredarcana.entity.MagicMissileEntity;
import com.bobroflza.tieredarcana.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/** The actual effects of each spell. All run server-side. */
final class Spells {
    private static final DustParticleOptions ARCANE_DUST = new DustParticleOptions(new Vector3f(0.78f, 0.49f, 1f), 1.2f);

    private Spells() {}

    private static BlockHitResult rayBlock(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 end = eye.add(p.getLookAngle().scale(range));
        return p.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
    }

    private static List<LivingEntity> livingAround(ServerLevel level, Vec3 center, double r, ServerPlayer except) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(r),
                e -> e != except && e.isAlive() && e.distanceToSqr(center) <= r * r);
    }

    // ---------------- Tier 1 ----------------

    static boolean magicMissile(ServerPlayer p, ServerLevel level, float power) {
        MagicMissileEntity missile = new MagicMissileEntity(level, p, 5f * power);
        missile.shootFromRotation(p, p.getXRot(), p.getYRot(), 0f, 2.2f, 0.3f);
        level.addFreshEntity(missile);
        level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8f, 1.6f);
        return true;
    }

    static boolean lightOrb(ServerPlayer p, ServerLevel level, float power) {
        BlockHitResult hit = rayBlock(p, 20);
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        if (!level.getBlockState(pos).canBeReplaced() || !level.getFluidState(pos).isEmpty()) return false;
        level.setBlock(pos, ModBlocks.LIGHT_ORB.get().defaultBlockState(), 3);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.02);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1.4f);
        return true;
    }

    static boolean minorHeal(ServerPlayer p, ServerLevel level, float power) {
        if (p.getHealth() >= p.getMaxHealth()) return false;
        p.heal(4f * power);
        level.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + 1.2, p.getZ(), 5, 0.4, 0.4, 0.4, 0.05);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 15, 0.5, 0.6, 0.5, 0.05);
        level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 0.8f);
        return true;
    }

    // ---------------- Tier 2 ----------------

    static boolean fireball(ServerPlayer p, ServerLevel level, float power) {
        Vec3 look = p.getLookAngle();
        SmallFireball fb = new SmallFireball(level, p, look.x, look.y, look.z);
        fb.setPos(p.getX() + look.x, p.getEyeY() - 0.1 + look.y, p.getZ() + look.z);
        level.addFreshEntity(fb);
        level.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1f, 1f);
        return true;
    }

    static boolean frostNova(ServerPlayer p, ServerLevel level, float power) {
        double r = 6 * power;
        for (LivingEntity e : livingAround(level, p.position(), r, p)) {
            e.hurt(level.damageSources().indirectMagic(p, p), 4f * power);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            e.setTicksFrozen(Math.max(e.getTicksFrozen(), 200));
        }
        for (int i = 0; i < 48; i++) {
            double a = i * Math.PI * 2 / 48;
            level.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.5, p.getZ(), 0,
                    Math.cos(a), 0.05, Math.sin(a), 0.6);
        }
        level.playSound(null, p.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1f, 0.6f);
        return true;
    }

    static boolean blink(ServerPlayer p, ServerLevel level, float power) {
        double range = 8 * power;
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        Vec3 target = hit.getLocation().subtract(look.scale(0.6)).subtract(0, p.getEyeHeight(), 0);
        Vec3 delta = target.subtract(p.position());
        Vec3 chosen = null;
        for (int up = 0; up <= 2; up++) {
            Vec3 d = delta.add(0, up, 0);
            if (level.noCollision(p, p.getBoundingBox().move(d))) {
                chosen = p.position().add(d);
                break;
            }
        }
        if (chosen == null || chosen.distanceToSqr(p.position()) < 1) return false;
        level.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 40, 0.3, 0.6, 0.3, 0.3);
        p.teleportTo(chosen.x, chosen.y, chosen.z);
        p.fallDistance = 0;
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, chosen.x, chosen.y + 1, chosen.z, 40, 0.3, 0.6, 0.3, 0.05);
        level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1.2f);
        return true;
    }

    static boolean growth(ServerPlayer p, ServerLevel level, float power) {
        BlockHitResult hit = rayBlock(p, 12);
        BlockPos center = hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : p.blockPosition();
        int r = Math.round(2 * power);
        boolean any = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockPos fixed = pos.immutable();
            if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, fixed, p)) {
                level.levelEvent(1505, fixed, 0);
                any = true;
            }
        }
        if (any) level.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1f, 1f);
        return any;
    }

    // ---------------- Tier 3 ----------------

    static boolean lightning(ServerPlayer p, ServerLevel level, float power) {
        BlockHitResult hit = rayBlock(p, 32);
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return false;
        bolt.moveTo(Vec3.atBottomCenterOf(hit.getBlockPos().above()));
        bolt.setCause(p);
        level.addFreshEntity(bolt);
        return true;
    }

    static boolean voidShield(ServerPlayer p, ServerLevel level, float power) {
        p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, Math.max(0, Math.round(power * 2) - 1)));
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 60, 0.6, 0.8, 0.6, 0.02);
        level.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 1.4f);
        return true;
    }

    static boolean levitate(ServerPlayer p, ServerLevel level, float power) {
        p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, Math.round(40 * power), 0));
        p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, Math.round(300 * power), 0));
        level.sendParticles(ARCANE_DUST, p.getX(), p.getY() + 0.2, p.getZ(), 30, 0.4, 0.1, 0.4, 0.02);
        level.playSound(null, p.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1f, 1.2f);
        return true;
    }

    // ---------------- Tier 4 ----------------

    static boolean meteor(ServerPlayer p, ServerLevel level, float power) {
        BlockHitResult hit = rayBlock(p, 48);
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        Vec3 target = Vec3.atCenterOf(hit.getBlockPos());
        LargeFireball meteor = new LargeFireball(level, p, 0, -1, 0, Math.max(2, Math.round(3 * power)));
        meteor.setPos(target.x, target.y + 30, target.z);
        level.addFreshEntity(meteor);
        level.playSound(null, hit.getBlockPos(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.5f, 0.6f);
        return true;
    }

    static boolean voidRift(ServerPlayer p, ServerLevel level, float power) {
        BlockHitResult hit = rayBlock(p, 24);
        Vec3 center = hit.getLocation().add(0, 1, 0);
        for (LivingEntity e : livingAround(level, center, 8, p)) {
            Vec3 pull = center.subtract(e.position()).normalize().scale(0.9);
            e.setDeltaMovement(e.getDeltaMovement().add(pull));
            e.hurtMarked = true;
            e.hurt(level.damageSources().indirectMagic(p, p), 6f * power);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 150, 2, 1.5, 2, 0.1);
        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 30, 0.4, 0.4, 0.4, 0.02);
        level.playSound(null, BlockPos.containing(center), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.6f, 1.6f);
        return true;
    }
}
