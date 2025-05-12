package com.sinsang.manager;

import com.sinsang.SinsangWarPlugin;
import com.sinsang.entity.SinsangEntity;
import com.sinsang.utils.WebhookUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SinsangManager implements Listener {

    private static final Map<String, SinsangEntity> activeSinsangs = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();
    private static boolean warActive = false;

    public static void reloadConfig() {
        SinsangWarPlugin.getInstance().reloadConfig();
    }

    public static void spawnSinsang(String name, CommandSender sender) {
        ConfigurationSection sinsangConfig = getSinsangConfig(name);
        if (sinsangConfig == null) {
            sender.sendMessage("§c해당 신상 이름이 없습니다!");
            return;
        }
        Location location = ((Player) sender).getLocation();
        String displayName = sinsangConfig.getString("displayName");
        double health = sinsangConfig.getDouble("health");

        SinsangEntity sinsang = new SinsangEntity(location, displayName, health);
        activeSinsangs.put(name, sinsang);
        sender.sendMessage("§a신상 소환 완료: " + displayName);
    }

    public static void removeSinsang(String name, CommandSender sender) {
        SinsangEntity sinsang = activeSinsangs.remove(name);
        if (sinsang != null) {
            sinsang.getStand().remove();
            sender.sendMessage("§a신상 제거 완료: " + name);
        } else {
            sender.sendMessage("§c해당 이름의 신상이 없습니다!");
        }
    }

    public static void listSinsangs(CommandSender sender) {
        if (activeSinsangs.isEmpty()) {
            sender.sendMessage("§c현재 소환된 신상이 없습니다.");
            return;
        }
        sender.sendMessage("§a현재 활성화된 신상 목록:");
        for (String key : activeSinsangs.keySet()) {
            sender.sendMessage("- " + key);
        }
    }

    public static void startWar(CommandSender sender) {
        warActive = true;
        sender.sendMessage("§a전쟁이 시작되었습니다!");
    }

    public static void stopWar(CommandSender sender) {
        warActive = false;
        sender.sendMessage("§c전쟁이 종료되었습니다!");

        // 최종 점령 정보 정리
        WebhookUtil.sendFinalResultWebhook();
    }

    private static ConfigurationSection getSinsangConfig(String name) {
        for (Object section : SinsangWarPlugin.getInstance().getConfig().getConfigurationSection("sinsangs").getValues(false).values()) {
            if (section instanceof ConfigurationSection && name.equalsIgnoreCase(((ConfigurationSection) section).getString("name"))) {
                return (ConfigurationSection) section;
            }
        }
        return null;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ArmorStand stand = (ArmorStand) event.getEntity();

        String found = null;
        for (Map.Entry<String, SinsangEntity> entry : activeSinsangs.entrySet()) {
            if (entry.getValue().getStand().equals(stand)) {
                found = entry.getKey();
                break;
            }
        }

        if (found == null) return;

        if (!warActive) {
            player.sendMessage("§c전쟁 중이 아닙니다! 지금은 신상을 공격할 수 없습니다.");
            event.setCancelled(true);
            return;
        }

        // 빠른 공격 방지
        long now = System.currentTimeMillis();
        if (lastHitTime.containsKey(player.getUniqueId())) {
            long last = lastHitTime.get(player.getUniqueId());
            if (now - last < 200) { // 5회 이상 빠르면
                WebhookUtil.sendError("빠른 점령 시도: " + player.getName() + " -> " + found);
                player.sendMessage("§c너무 빠르게 공격하고 있습니다!");
                event.setCancelled(true);
                return;
            }
        }
        lastHitTime.put(player.getUniqueId(), now);

        // 데미지 적용
        SinsangEntity sinsang = activeSinsangs.get(found);
        sinsang.damage(event.getDamage());

        if (sinsang.getHealth() <= 0) {
            activeSinsangs.remove(found);
            stand.remove();
            Bukkit.broadcastMessage("§a" + player.getName() + " 플레이어가 " + sinsang.getStand().getCustomName() + " 신상을 점령했습니다!");
            WebhookUtil.sendCaptureWebhook(player.getName(), sinsang.getStand().getCustomName());
        }
    }
}
