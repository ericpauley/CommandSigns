package org.zonedabone.commandsigns.handlers;

import org.bukkit.ChatColor;
import org.zonedabone.commandsigns.CommandSignExecutor;
import org.zonedabone.commandsigns.CommandSigns;

public class MoneyHandler extends Handler {

	@Override
	public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
		if (e.getPlayer() != null && CommandSigns.economy != null && CommandSigns.economy.isEnabled() && command.startsWith("$")) {
			double amount = 0;
			try {
				amount = Double.parseDouble(command.substring(1));
			} catch (NumberFormatException ex) {
				return;
			}
			if (CommandSigns.economy.withdrawPlayer(e.getPlayer().getName(), amount).transactionSuccess() ^ negate) {
				e.getRestrictions().push(true);
			} else {
				e.getRestrictions().push(false);
				if (!silent)
					e.getPlayer().sendMessage(ChatColor.RED + "You cannot afford to use this CommandSign. (" + CommandSigns.economy.format(amount) + ")");
			}
		}
	}

}
