package org.zonedabone.commandsigns.handlers;

import org.zonedabone.commandsigns.CommandSignExecutor;

public class ChatHandler extends Handler {
    
    @Override
    public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
        if (e.getPlayer() != null && command.startsWith(".")) {
            command = command.substring(1);
            e.getPlayer().chat(command);
        }
    }
    
}
