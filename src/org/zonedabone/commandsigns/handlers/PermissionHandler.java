package org.zonedabone.commandsigns.handlers;

import org.bukkit.ChatColor;
import org.zonedabone.commandsigns.CommandSignExecutor;
import org.zonedabone.commandsigns.CommandSigns;

public class PermissionHandler extends Handler {
    
    @Override
    public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
        if (e.getPlayer() != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("&")) {
            boolean allowed = false;
            for(String s:command.substring(1).split(",")){
                allowed = allowed || e.getPlugin().hasPermission(e.getPlayer(), s);
            }
            if (allowed ^ negate) {
                e.getRestrictions().push(true);
            } else {
                e.getRestrictions().push(false);
                if (!silent)
                    e.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to use this CommandSign.");
            }
        }
    }
    
}
