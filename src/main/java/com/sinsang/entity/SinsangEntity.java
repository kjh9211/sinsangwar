package com.sinsang.entity;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

public class SinsangEntity {

    private final ArmorStand stand;
    private double health;

    public SinsangEntity(Location location, String displayName, double health) {
        this.health = health;

        this.stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        this.stand.setCustomName(displayName);
        this.stand.setCustomNameVisible(true);
        this.stand.setInvisible(false);
        this.stand.setInvulnerable(false);
        this.stand.setGravity(false);
        this.stand.setSmall(false);
    }

    public ArmorStand getStand() {
        return stand;
    }

    public double getHealth() {
        return health;
    }

    public void damage(double amount) {
        this.health -= amount;
    }
}
