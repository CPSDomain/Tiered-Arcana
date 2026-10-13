package com.bobroflza.tieredarcana.mana;

import com.bobroflza.tieredarcana.config.ArcanaConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Everything Tiered Arcana stores on a player: mana, max mana upgrades,
 * learned spells, the active spell, cooldowns and the highest shrine tier attuned.
 */
public class ArcanePlayerData {
    public static final Capability<ArcanePlayerData> CAP = CapabilityManager.get(new CapabilityToken<>() {});

    private float mana = -1; // -1 = not yet initialised, filled to max on first tick
    private int maxMana = 100;
    private int crystalsUsed;
    private int shrineTier;
    private final Set<ResourceLocation> knownSpells = new LinkedHashSet<>();
    @Nullable
    private ResourceLocation activeSpell;
    private final Map<ResourceLocation, Long> cooldownEnds = new HashMap<>();
    private long shrineBoostUntil;
    private int shrineBoostTier;
    private boolean dirty = true;

    public static LazyOptional<ArcanePlayerData> get(Player player) {
        return player.getCapability(CAP);
    }

    // ---- mana ----
    public float getMana() { return Math.max(mana, 0); }
    public int getMaxMana() { return maxMana; }

    public void recalcMax() {
        int newMax = ArcanaConfig.BASE_MAX_MANA.get() + crystalsUsed * ArcanaConfig.MANA_PER_CRYSTAL.get();
        if (newMax != maxMana) { maxMana = newMax; dirty = true; }
        if (mana < 0) { mana = maxMana; dirty = true; }
        if (mana > maxMana) { mana = maxMana; dirty = true; }
    }

    public void setMana(float value) {
        float clamped = Math.max(0, Math.min(value, maxMana));
        if (clamped != mana) { mana = clamped; dirty = true; }
    }

    public void addMana(float amount) { setMana(getMana() + amount); }

    public boolean consumeMana(float amount) {
        if (getMana() < amount) return false;
        setMana(getMana() - amount);
        return true;
    }

    // ---- crystals / shrine ----
    public int getCrystalsUsed() { return crystalsUsed; }
    public void addCrystal() { crystalsUsed++; recalcMax(); dirty = true; }
    public int getShrineTier() { return shrineTier; }

    public boolean attune(int tier) {
        if (tier > shrineTier) { shrineTier = tier; dirty = true; return true; }
        return false;
    }

    public void setShrineBoost(long until, int tier) { shrineBoostUntil = until; shrineBoostTier = tier; }
    public boolean hasShrineBoost(long now) { return now < shrineBoostUntil; }
    public int getShrineBoostTier() { return shrineBoostTier; }

    // ---- spells ----
    public Set<ResourceLocation> getKnownSpells() { return Collections.unmodifiableSet(knownSpells); }
    public boolean knows(ResourceLocation id) { return knownSpells.contains(id); }

    public boolean learn(ResourceLocation id) {
        if (knownSpells.add(id)) {
            if (activeSpell == null) activeSpell = id;
            dirty = true;
            return true;
        }
        return false;
    }

    @Nullable public ResourceLocation getActiveSpell() { return activeSpell; }

    public void setActiveSpell(@Nullable ResourceLocation id) {
        if (!Objects.equals(id, activeSpell)) { activeSpell = id; dirty = true; }
    }

    public long getCooldownEnd(ResourceLocation id) { return cooldownEnds.getOrDefault(id, 0L); }
    public void setCooldownEnd(ResourceLocation id, long end) { cooldownEnds.put(id, end); dirty = true; }
    public Map<ResourceLocation, Long> getCooldownEnds() { return Collections.unmodifiableMap(cooldownEnds); }

    // ---- sync flag ----
    public boolean isDirty() { return dirty; }
    public void markDirty() { dirty = true; }
    public void clearDirty() { dirty = false; }

    // ---- persistence ----
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Mana", mana);
        tag.putInt("Crystals", crystalsUsed);
        tag.putInt("ShrineTier", shrineTier);
        ListTag list = new ListTag();
        for (ResourceLocation id : knownSpells) list.add(StringTag.valueOf(id.toString()));
        tag.put("Known", list);
        if (activeSpell != null) tag.putString("Active", activeSpell.toString());
        return tag;
    }

    public void load(CompoundTag tag) {
        mana = tag.contains("Mana") ? tag.getFloat("Mana") : -1;
        crystalsUsed = tag.getInt("Crystals");
        shrineTier = tag.getInt("ShrineTier");
        knownSpells.clear();
        ListTag list = tag.getList("Known", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) knownSpells.add(id);
        }
        activeSpell = tag.contains("Active") ? ResourceLocation.tryParse(tag.getString("Active")) : null;
        dirty = true;
    }

    public void copyFrom(ArcanePlayerData other, boolean death) {
        load(other.save());
        maxMana = other.maxMana;
        if (death) mana = other.maxMana / 2f;
        dirty = true;
    }
}
