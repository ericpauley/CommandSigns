package org.zonedabone.commandsigns.handler;

import java.util.Map;

import org.zonedabone.commandsigns.SignExecutor;

public class CooldownHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		plugin = e.getPlugin();
		if (e.getPlayer() != null && command.startsWith("~")) {
			int amount = 0;
			if (command.length() > 1) {
				try {
					amount = (int) (Double.parseDouble(command.substring(1)) * 1000);
				} catch (NumberFormatException ex) {
					return;
				}
			}

			Map<String, Long> lastUse = e.getText().getTimeouts();
			Long latest = lastUse.get(e.getPlayer().getName());
			// If condition is true and negate is true, reject
			// If condition is false and negate is false, reject
			// If condition is true and negate is false or vice versa, accept
			// (XOR)
			if (amount != 0) {
				if ((latest == null || System.currentTimeMillis() - latest > amount)
						^ negate) {
					lastUse.put(e.getPlayer().getName(),
							System.currentTimeMillis());
					// Set the current command block to be enabled
					e.getRestrictions().push(true);
				} else {
					// Set the current command block to be denied
					e.getRestrictions().push(false);
					// Show error if not silent
					if (!silent) {
						if (negate)
							plugin.messenger.sendMessage(e.getPlayer(),
									"restriction.inverse_cooldown",
									new String[] { "COOLDOWN" },
									new String[] { "" + amount / 1000 });
						else
							plugin.messenger
									.sendMessage(
											e.getPlayer(),
											"restriction.cooldown",
											new String[] { "COOLDOWN" },
											new String[] { ""
													+ Math.round((amount
															+ latest - System
															.currentTimeMillis()) / 1000 + 1) });
					}
					if (negate) {
					    long time = System.currentTimeMillis();
                        lastUse.put(e.getPlayer().getName(), time);
                        latest = time;
					}
				}
			} else {
				if ((latest == null) ^ negate) {
					lastUse.put(e.getPlayer().getName(),
							System.currentTimeMillis());
					// Set the current command block to be enabled
					e.getRestrictions().push(true);

				} else {
					e.getRestrictions().push(false);
					// Show error if not silent
					if (!silent) {
						if (negate)
							plugin.messenger.sendMessage(e.getPlayer(),
									"restriction.inverse_use_once");
						else
							plugin.messenger.sendMessage(e.getPlayer(),
									"restriction.use_once");
					}
					if (negate) {
					    long time = System.currentTimeMillis();
						lastUse.put(e.getPlayer().getName(), time);
						latest = time;
					}
				}
			}

		}
	}

}
