package org.zonedabone.commandsigns.handler;

import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.SignExecutor;

public abstract class Handler {

	protected CommandSigns plugin;
	
	public abstract void handle(SignExecutor e, String command, boolean silent,
			boolean negate);

}
