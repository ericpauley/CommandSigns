package org.zonedabone.commandsigns.handlers;

import org.bukkit.event.block.Action;
import org.zonedabone.commandsigns.CommandSignExecutor;

public class ClickTypeHandler extends Handler {

	@Override
	public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
		if (e.getPlayer() != null && command.startsWith(">>")) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK ^ negate) {
				e.getRestrictions().push(true);
			} else {
				e.getRestrictions().push(false);
			}
		}
	}

}
