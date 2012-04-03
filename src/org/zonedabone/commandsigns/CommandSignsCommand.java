package org.zonedabone.commandsigns;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandSignsCommand implements CommandExecutor {
	
	private CommandSigns plugin;
	
	public CommandSignsCommand(CommandSigns plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("commandsigns")) {
			if (args.length < 1) {
				return false;
			}
			Player player = (Player) sender;
			String playerName = player.getName();
			if (args[0].indexOf("line") == 0) {
				if (plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super", false)) {
					int lineNumber;
					try {
						lineNumber = Integer.parseInt(args[0].substring(4));
					} catch (NumberFormatException ex) {
						player.sendMessage("Line number invalid!");
						return true;
					}
					CommandSignsText text;
					if ((text = plugin.playerText.get(playerName)) == null) {
						text = new CommandSignsText(player.getName());
					}
					String line = "";
					for (int i = 1; i < args.length; i++) {
						line = line.concat(args[i] + (i != args.length - 1 ? " " : ""));
					}
					if ((line.contains("/*") || line.contains("/^") || line.contains("/#")) && !plugin.hasPermission(player, "commandsigns.create.super", false)) {
						while (line.contains("/*")) {
							line = line.replace("/*", "/");
						}
						while (line.contains("/^")) {
							line = line.replace("/^", "/");
						}
						while (line.contains("/#")) {
							line = line.replace("#", "/");
						}
						player.sendMessage("You may not make signs with '/*', '/^', or '/#'");
					}
					text.setLine(lineNumber, line);
					plugin.playerText.put(playerName, text);
					player.sendMessage("Line " + lineNumber + ": " + line);
					plugin.playerStates.put(playerName, CommandSignsPlayerState.ENABLE);
					player.sendMessage("Ready to add.");
				} else {
					player.sendMessage(ChatColor.RED + "You do not have perisssion.");
				}
			} else if (args[0].equalsIgnoreCase("read")) {
				if (plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super", false)) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.READ);
					player.sendMessage("Click a sign to read CommandSign text.");
				} else {
					player.sendMessage(ChatColor.RED + "You do not have perisssion.");
				}
			} else if (args[0].equalsIgnoreCase("copy")) {
				if (plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super", false)) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.COPY);
					player.sendMessage("Click a sign to copy CommandSign text.");
				} else {
					player.sendMessage(ChatColor.RED + "You do not have perisssion.");
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (plugin.hasPermission(player, "commandsigns.remove")) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.DISABLE);
					player.sendMessage("Click a sign to remove CommandSign.");
				}
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (plugin.hasPermission(player, "commandsigns.remove", false) || plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super", false)) {
					plugin.playerStates.remove(playerName);
					plugin.playerText.remove(playerName);
					player.sendMessage("CommandSign text and status cleared.");
				} else {
					player.sendMessage(ChatColor.RED + "You do not have perisssion.");
				}
			} else {
				player.sendMessage("Wrong CommandSigns command syntax.");
			}
			return true;
		}
		return false;
	}
}
