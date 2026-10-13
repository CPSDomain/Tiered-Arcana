package com.bobroflza.tieredarcana.spell;

import net.minecraft.network.chat.Component;

public enum School {
    FIRE(0xFF7A33),
    FROST(0x7FE6FF),
    ARCANE(0xC77DFF),
    NATURE(0x6BD45A),
    VOID(0x9A5CFF);

    private final int color;

    School(int color) { this.color = color; }

    public int getColor() { return color; }

    public Component getName() {
        return Component.translatable("school.tieredarcana." + name().toLowerCase());
    }
}
