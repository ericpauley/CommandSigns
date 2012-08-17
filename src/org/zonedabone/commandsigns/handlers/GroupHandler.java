package org.zonedabone.commandsigns.handlers;

import org.bukkit.ChatColor;
import org.zonedabone.commandsigns.CommandSignExecutor;
import org.zonedabone.commandsigns.CommandSigns;

public class GroupHandler extends Handler {

	@Override
	public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
		if (e.getPlayer() != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("@")) {
			if (CommandSigns.permission.playerInGroup(e.getPlayer(), command.substring(1)) ^ negate) {
				e.getRestrictions().push(true);
			} else {
				e.getRestrictions().push(false);
				if (!silent)
					e.getPlayer().sendMessage(ChatColor.RED + "You are not in the required group to run this command.");
			}
		}
	}

}
