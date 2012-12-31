package org.zonedabone.commandsigns.handler;

import org.zonedabone.commandsigns.SignExecutor;

public abstract class Handler {

	public abstract void handle(SignExecutor e, String command,
			boolean silent, boolean negate);

}
