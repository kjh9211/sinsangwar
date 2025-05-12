package com.sinsang;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class SinsangWarPlugin extends JavaPlugin {

    private static SinsangWarPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Objects.requireNonNull(getCommand("sinsang")).setExecutor(new com.sinsang.command.SinsangCommand());
        getServer().getPluginManager().registerEvents(new com.sinsang.manager.SinsangManager(), this);
        getLogger().info("SinsangWarPlugin 활성화 완료!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SinsangWarPlugin 비활성화 완료!");
    }

    public static SinsangWarPlugin getInstance() {
        return instance;
    }
}
