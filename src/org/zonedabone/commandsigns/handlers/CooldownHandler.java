package org.zonedabone.commandsigns.handlers;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.zonedabone.commandsigns.CommandSignExecutor;

public class CooldownHandler extends Handler {

	public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
		if (e.getPlayer() != null && command.startsWith("~")) {
			int amount = 0;
			try {
				amount = (int) (Double.parseDouble(command.substring(1)) * 1000);
			} catch (NumberFormatException ex) {
				return;
			}
			Map<OfflinePlayer, Long> lastUse = e.getPlugin().getSignTimeouts(e.getLocation());
			Long latest = lastUse.get(e.getPlayer());
			// If condition is true and negate is true, reject
			// If condition is false and negate is false, reject
			// If condition is true and negate is false or vice versa, accept
			// (XOR)
			if ((latest == null || System.currentTimeMillis() - latest > amount) ^ negate) {
				lastUse.put(e.getPlayer(), System.currentTimeMillis());
				// Set the current command block to be enabled
				e.getRestrictions().push(true);
			} else {
				// Set the current command block to be denied
				e.getRestrictions().push(false);
				// Show error if not silent
				if (!silent) {
					if (negate)
						e.getPlayer().sendMessage(ChatColor.RED + "You must click again within " + amount / 1000 + " seconds to use this sign.");
					else
						e.getPlayer().sendMessage(ChatColor.RED + "You must wait another " + Math.round((amount + latest - System.currentTimeMillis()) / 1000 + 1) + " seconds before using this sign again.");
				}
				if (negate) {
					lastUse.put(e.getPlayer(), System.currentTimeMillis());
					latest = lastUse.get(e.getPlayer());
				}
			}
		}
	}

}
