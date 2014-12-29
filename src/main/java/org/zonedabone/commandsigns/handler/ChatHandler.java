package org.zonedabone.commandsigns.handler;

import org.zonedabone.commandsigns.SignExecutor;

public class ChatHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		if (e.getPlayer() != null && command.startsWith(".")) {
			command = command.substring(1);
			e.getPlayer().chat(command);
		}
	}

}
