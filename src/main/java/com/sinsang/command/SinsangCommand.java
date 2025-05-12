package com.sinsang.command;

import com.sinsang.manager.SinsangManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SinsangCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("sinsang.admin")) {
            sender.sendMessage("§c권한이 없습니다!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§e/sinsang [reload|spawn|remove|list|war]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                SinsangManager.reloadConfig();
                sender.sendMessage("§a설정이 리로드되었습니다.");
                break;
            case "spawn":
                if (args.length >= 2) {
                    SinsangManager.spawnSinsang(args[1], sender);
                } else {
                    sender.sendMessage("§e/sinsang spawn <신상이름>");
                }
                break;
            case "remove":
                if (args.length >= 2) {
                    SinsangManager.removeSinsang(args[1], sender);
                } else {
                    sender.sendMessage("§e/sinsang remove <신상이름>");
                }
                break;
            case "list":
                SinsangManager.listSinsangs(sender);
                break;
            case "war":
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("start")) {
                        SinsangManager.startWar(sender);
                    } else if (args[1].equalsIgnoreCase("stop")) {
                        SinsangManager.stopWar(sender);
                    } else {
                        sender.sendMessage("§e/sinsang war [start|stop]");
                    }
                } else {
                    sender.sendMessage("§e/sinsang war [start|stop]");
                }
                break;
            default:
                sender.sendMessage("§e/sinsang [reload|spawn|remove|list|war]");
        }

        return true;
    }
}
