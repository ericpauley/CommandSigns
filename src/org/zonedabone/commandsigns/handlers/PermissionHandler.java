package org.zonedabone.commandsigns.handlers;

import org.bukkit.ChatColor;
import org.zonedabone.commandsigns.CommandSignExecutor;
import org.zonedabone.commandsigns.CommandSigns;

public class PermissionHandler extends Handler {
    
    @Override
    public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
        if (e.getPlayer() != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("&")) {
            if (e.getPlugin().hasPermission(e.getPlayer(), command.substring(1), false) ^ negate) {
                e.getRestrictions().push(true);
            } else {
                e.getRestrictions().push(false);
                if (!silent)
                    e.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to use this CommandSign.");
            }
        }
    }
    
}
