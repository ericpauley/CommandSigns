package org.zonedabone.commandsigns.handler;

import java.util.Map;

import org.bukkit.ChatColor;
import org.zonedabone.commandsigns.SignExecutor;

public class CooldownHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
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
							e.getPlayer().sendMessage(
									ChatColor.RED
											+ "You must click again within "
											+ amount / 1000
											+ " seconds to use this sign.");
						else
							e.getPlayer()
									.sendMessage(
											ChatColor.RED
													+ "You must wait another "
													+ Math.round((amount
															+ latest - System
															.currentTimeMillis()) / 1000 + 1)
													+ " seconds before using this sign again.");
					}
					if (negate) {
						lastUse.put(e.getPlayer().getName(),
								System.currentTimeMillis());
						latest = lastUse.get(e.getPlayer());
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
							e.getPlayer()
									.sendMessage(
											ChatColor.RED
													+ "You must use this sign before you can use this sign... I'm confused.");
						else
							e.getPlayer()
									.sendMessage(
											ChatColor.RED
													+ "You can only use this sign once.");
					}
					if (negate) {
						lastUse.put(e.getPlayer().getName(),
								System.currentTimeMillis());
						latest = lastUse.get(e.getPlayer());
					}
				}
			}

		}
	}

}
