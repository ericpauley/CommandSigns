package org.zonedabone.commandsigns.handler;

import org.zonedabone.commandsigns.SignExecutor;

public class RandomHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		plugin = e.getPlugin();
		if (command.startsWith("`")) {
			double amount = 0;
			try {
				amount = Double.parseDouble(command.substring(1));
			} catch (NumberFormatException ex) {
				return;
			}
			amount /= 100;
			if ((Math.random() < amount) ^ negate) {
				e.getRestrictions().push(true);
			} else {
				e.getRestrictions().push(false);
				if (!silent && e.getPlayer() != null)
					plugin.messenger.sendMessage(e.getPlayer(),
							"restriction.bad_random");
			}
		}

	}

}
