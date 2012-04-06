package org.zonedabone.commandsigns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
			final Player player = (Player) sender;
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
					CommandSignsText text = plugin.playerText.get(playerName);
					if (text == null) {
						text = new CommandSignsText(player.getName());
						plugin.playerText.put(playerName, text);
					}
					String line = "";
					for (int i = 1; i < args.length; i++) {
						line = line.concat((i == 0 ? "" : " ") + args[i]);
					}
					if ((line.startsWith("/*") || line.startsWith("/^") || line.startsWith("/#")) && !plugin.hasPermission(player, "commandsigns.create.super", false)) {
						line = "/" + line.substring(2);
						player.sendMessage("You may not make signs with '/*', '/^', or '/#'");
					}
					text.setLine(lineNumber, line);
					text.trim();
					player.sendMessage(lineNumber + ": " + line);
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
			} else if (args[0].equalsIgnoreCase("update")) {
				if (player.hasPermission("commandsigns.update")) {
					if (plugin.version<plugin.newestVersion) {
						if (!plugin.getUpdateFile().exists()) {
							player.sendMessage(ChatColor.YELLOW + "Updating CommandSigns to version " + ChatColor.DARK_PURPLE + plugin.stringNew + ChatColor.YELLOW + "...");
							new Thread() {
								
								@Override
								public void run() {
									try {
										long startTime = System.currentTimeMillis();
										URL url = new URL(plugin.downloadLocation);
										url.openConnection();
										InputStream reader = url.openStream();
										File f = plugin.getUpdateFile();
										f.getParentFile().mkdirs();
										FileOutputStream writer = new FileOutputStream(f);
										byte[] buffer = new byte[153600];
										int totalBytesRead = 0;
										int bytesRead = 0;
										while ((bytesRead = reader.read(buffer)) > 0) {
											writer.write(buffer, 0, bytesRead);
											buffer = new byte[153600];
											totalBytesRead += bytesRead;
										}
										long endTime = System.currentTimeMillis();
										player.sendMessage(ChatColor.YELLOW + "Downloaded " + ChatColor.DARK_PURPLE + (((totalBytesRead)) / 1000) + ChatColor.YELLOW + " KB in " + ChatColor.DARK_PURPLE + (((double) (endTime - startTime)) / 1000) + ChatColor.YELLOW + " seconds. To complete the update, restart/reload your server.");
										writer.close();
										reader.close();
									} catch (MalformedURLException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}.start();
						} else {
							player.sendMessage(ChatColor.YELLOW + "An update to CommandSigns is already waiting to be installed on reload/restart.");
						}
					} else {
						player.sendMessage(ChatColor.YELLOW + "CommandSigns is already up to date. (" + ChatColor.DARK_PURPLE + plugin.getDescription().getVersion() + ChatColor.YELLOW + ")");
					}
				}
			} else {
				player.sendMessage("Wrong CommandSigns command syntax.");
			}
			return true;
		}
		return false;
	}
}
