package com.example.ewa;

/** Ability slots with their base Essence cost and cooldown (in ticks). */
public enum Slot {
    POWER1(10, 3 * 20),
    POWER2(15, 6 * 20),
    POWER3(20, 10 * 20),
    CONDUIT(30, 15 * 20);

    public final double cost;
    public final long cooldownTicks;

    Slot(double cost, long cooldownTicks) {
        this.cost = cost;
        this.cooldownTicks = cooldownTicks;
    }
}
