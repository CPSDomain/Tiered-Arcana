package com.bobroflza.tieredarcana.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class Spell {
    private final ResourceLocation id;
    private final School school;
    private final int tier;
    private final int manaCost;
    private final int cooldownTicks;

    protected Spell(ResourceLocation id, School school, int tier, int manaCost, int cooldownTicks) {
        this.id = id;
        this.school = school;
        this.tier = tier;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
    }

    /**
     * Perform the spell on the server.
     *
     * @param power wand power multiplier (1.0 for the Apprentice Wand)
     * @return true if the spell actually did something (mana and cooldown are only spent then)
     */
    public abstract boolean cast(ServerPlayer player, ServerLevel level, float power);

    public ResourceLocation getId() { return id; }
    public School getSchool() { return school; }
    public int getTier() { return tier; }
    public int getManaCost() { return manaCost; }
    public int getCooldownTicks() { return cooldownTicks; }

    public MutableComponent getName() {
        return Component.translatable("spell." + id.getNamespace() + "." + id.getPath())
                .withStyle(Style.EMPTY.withColor(school.getColor()));
    }

    public Component getDescription() {
        return Component.translatable("spell." + id.getNamespace() + "." + id.getPath() + ".desc");
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation(id.getNamespace(), "textures/gui/spells/" + id.getPath() + ".png");
    }
}
