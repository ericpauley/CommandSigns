package org.zonedabone.commandsigns.handler;

import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.SignExecutor;

public class MoneyHandler extends Handler {

	@SuppressWarnings("deprecation")
	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		plugin = e.getPlugin();
		if (e.getPlayer() != null && CommandSigns.economy != null
				&& CommandSigns.economy.isEnabled() && command.startsWith("$")) {
			double amount = 0;
			try {
				amount = Double.parseDouble(command.substring(1));
			} catch (NumberFormatException ex) {
				return;
			}
			if (CommandSigns.economy.withdrawPlayer(e.getPlayer().getName(),
					amount).transactionSuccess()
					^ negate) {
				e.getRestrictions().push(true);
			} else {
				e.getRestrictions().push(false);
				if (!silent)
					plugin.messenger.sendMessage(e.getPlayer(),
							"restriction.not_enough_money",
							new String[] { "MONEY" }, new String[] { ""
									+ CommandSigns.economy.format(amount) });
			}
		}
	}

}
