package org.zonedabone.commandsigns.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.zonedabone.commandsigns.CommandSignExecutor;
import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.CommandSignsProxy;
import org.zonedabone.commandsigns.Messaging;

public class CommandHandler extends Handler {

	@Override
	public void handle(CommandSignExecutor e, String command, boolean silent, boolean negate) {
		if (command.startsWith("/") || command.startsWith("\\")) {
			boolean op = false;
			List<PermissionAttachment> given = new ArrayList<PermissionAttachment>();
			List<String> vGiven = new ArrayList<String>();
			Player player = e.getPlayer();
			CommandSigns plugin = e.getPlugin();
			if(command.startsWith("/")) {
				command = command.substring(1);
				if (command.length() == 0) {
					return;
				}
				if (player != null) {
					try {
						if (command.startsWith("*")) {
							command = command.substring(1);
							if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
								// This needs to be fixed!
								for (Map.Entry<String, Boolean> s : Bukkit.getPluginManager().getPermission("commandsigns.permissions").getChildren().entrySet()) {
									given.add(player.addAttachment(plugin, s.getKey(), s.getValue()));
									if (CommandSigns.permission.playerHas(player, s.getKey()) != s.getValue()) {
										String node = (s.getValue() ? "" : "-") + s.getKey();
										CommandSigns.permission.playerAdd(player, node);
										vGiven.add(node);
									}
								}
								given.add(player.addAttachment(plugin, "commandsigns.permissions", true));
								player.performCommand(command);
								
								// Sending commands this way allows it to be sent 'silently'
								// BUT for commands like /warp, it gives an error. Not sure why.
								//CommandSender cs = new CommandSignsProxy(player, player, silent);
								//plugin.getServer().dispatchCommand(cs, command);
							} else {
								if (!silent)
									Messaging.sendMessage(player, "cannot_use");
								return;
							}
						} else if (command.startsWith("^")) {
							command = command.substring(1);
							if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
								if (!player.isOp()) {
									op = true;
									player.setOp(true);
								}
								player.performCommand(command);
								//CommandSender cs = new CommandSignsProxy(player, player, silent);
								//plugin.getServer().dispatchCommand(cs, command);
							} else {
								if (!silent)
									Messaging.sendMessage(player, "cannot_use");
								return;
							}
						} else if (command.startsWith("#")) {
							command = command.substring(1);
							if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
								CommandSender cs = new CommandSignsProxy(plugin.getServer().getConsoleSender(), player, silent);
								plugin.getServer().dispatchCommand(cs, command);
							} else {
								if (!silent)
									Messaging.sendMessage(player, "cannot_use");
								return;
							}
						} else {
							player.performCommand(command);
							//CommandSender sender = new CommandSignsProxy(player, player, silent);
							//Bukkit.dispatchCommand(sender, command);
							
							/*String[] splitCommand = command.split(" ");
							String[] args = new String[splitCommand.length - 1];
							if (splitCommand.length > 0) {
								for(int i=1; i < splitCommand.length; i++) args[i-1] = splitCommand[i];
						        Command target = plugin.getServer().getPluginCommand(splitCommand[0]);
						        target.execute(sender, splitCommand[0], args);
					        }*/
					        
						}
					} finally {
						for (PermissionAttachment pa : given) {
							pa.remove();
						}
						for (String s : vGiven) {
							CommandSigns.permission.playerRemove(player, s);
						}
						if (op) {
							player.setOp(false);
						}
					}
				} else {
					if (command.startsWith("*") || command.startsWith("^") || command.startsWith("#")) {
						command = command.substring(1);
					}
					CommandSender cs = new CommandSignsProxy(plugin.getServer().getConsoleSender(), plugin.getServer().getConsoleSender(), silent);
					plugin.getServer().dispatchCommand(cs, command);
				}
			}
		}
	}

}
