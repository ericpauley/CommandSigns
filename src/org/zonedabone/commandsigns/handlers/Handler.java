package org.zonedabone.commandsigns.handlers;

import org.zonedabone.commandsigns.CommandSignExecutor;

public abstract class Handler {

	public abstract void handle(CommandSignExecutor e, String command, boolean silent, boolean negate);
	
}
