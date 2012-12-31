package org.zonedabone.commandsigns.handler;

import org.bukkit.ChatColor;
import org.zonedabone.commandsigns.SignExecutor;

public class SendHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		if (e.getPlayer() != null && command.startsWith("\\")) {
			command = command.substring(1);
			e.getPlayer().sendMessage(
					ChatColor.translateAlternateColorCodes('&', command));
		}
	}

}
