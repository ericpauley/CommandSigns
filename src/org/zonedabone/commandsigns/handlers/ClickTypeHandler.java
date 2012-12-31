package org.zonedabone.commandsigns.handlers;

import org.bukkit.event.block.Action;
import org.zonedabone.commandsigns.SignExecutor;

public class ClickTypeHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		if (e.getPlayer() != null) {
			if (command.startsWith(">>")) {
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK ^ negate) {
					e.getRestrictions().push(true);
				} else {
					e.getRestrictions().push(false);
				}
			} else if (command.startsWith("<<")) {
				if (e.getAction() == Action.LEFT_CLICK_BLOCK ^ negate) {
					e.getRestrictions().push(true);
				} else {
					e.getRestrictions().push(false);
				}
			}
		}
	}

}
