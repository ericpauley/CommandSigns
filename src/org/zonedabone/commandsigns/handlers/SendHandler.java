package org.zonedabone.commandsigns.handlers;

import org.bukkit.ChatColor;
import org.zonedabone.commandsigns.CommandSignExecutor;

public class SendHandler extends Handler {

	@Override
	public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
		if (command.startsWith("\\")) {
			command = command.substring(1);
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', command));
		}
	}

}
