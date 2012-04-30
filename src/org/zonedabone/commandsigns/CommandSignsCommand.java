package org.zonedabone.commandsigns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
	public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("commandsigns")) {
			if (args.length < 1) {
				return false;
			}
			Player player = null;
			String playerName = null;
			if (sender instanceof Player) {
				player = (Player) sender;
				player.getName();
			}
			if (args[0].indexOf("line") == 0) {
				if (player == null) {
					Messaging.sendMessage(sender, "failure.player_only");
					return true;
				}
				if (plugin.hasPermission(player, "commandsigns.create.regular")) {
					int lineNumber;
					try {
						lineNumber = Integer.parseInt(args[0].substring(4));
					} catch (NumberFormatException ex) {
						Messaging.sendMessage(player, "failure.invalid_line");
						return true;
					}
					CommandSignsText text = plugin.playerText.get(playerName);
					if (text == null) {
						text = new CommandSignsText(player.getName(), false);
						plugin.playerText.put(playerName, text);
					}
					String line = "";
					for (int i = 1; i < args.length; i++) {
						line = line.concat((i == 0 ? "" : " ") + args[i]);
					}
					if ((line.startsWith("/*") || line.startsWith("/^") || line.startsWith("/#")) && !plugin.hasPermission(player, "commandsigns.create.super", false)) {
						Messaging.sendMessage(player, "failure.no_super");
						return true;
					}
					text.setLine(lineNumber, line);
					text.trim();
					String display = line.replace("$", "\\$");
					Messaging.sendRaw(player, "success.line_print", "n", "" + lineNumber, "l", display);
					plugin.playerStates.put(playerName, CommandSignsPlayerState.ENABLE);
					Messaging.sendMessage(player, "progress.add");
				}
			} else if (args[0].equalsIgnoreCase("redstone")) {
				if (player == null) {
					Messaging.sendMessage(sender, "failure.player_only");
					return true;
				}
				if (plugin.hasPermission(player, "commandsigns.create.redstone")) {
					CommandSignsText text = plugin.playerText.get(playerName);
					if (text == null) {
						text = new CommandSignsText(player.getName(), false);
						plugin.playerText.put(playerName, text);
					}
					text.setRedstone(!text.isRedstone());
					plugin.playerStates.put(playerName, CommandSignsPlayerState.ENABLE);
					Messaging.sendMessage(player, "progress.redstone", "s", ((text.isRedstone()) ? "enabled" : "disabled"));
					Messaging.sendMessage(player, "progress.add");
				}
			} else if (args[0].equalsIgnoreCase("read")) {
				if (player == null) {
					Messaging.sendMessage(sender, "failure.player_only");
					return true;
				}
				if (plugin.hasPermission(player, "commandsigns.create.regular")) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.READ);
					Messaging.sendMessage(player, "progress.read");
				}
			} else if (args[0].equalsIgnoreCase("copy")) {
				if (player == null) {
					Messaging.sendMessage(sender, "failure.player_only");
					return true;
				}
				if (plugin.hasPermission(player, "commandsigns.create.regular")) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.COPY);
					Messaging.sendMessage(player, "progress.copy");
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (player == null) {
					Messaging.sendMessage(sender, "failure.player_only");
					return true;
				}
				if (plugin.hasPermission(player, "commandsigns.remove")) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.DISABLE);
					Messaging.sendMessage(player, "progress.remove");
				}
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (player == null) {
					Messaging.sendMessage(sender, "failure.player_only");
					return true;
				}
				if (plugin.hasPermission(player, "commandsigns.remove")) {
					plugin.playerStates.remove(playerName);
					plugin.playerText.remove(playerName);
					Messaging.sendMessage(player, "success.cleared");
				}
			} else if (args[0].equalsIgnoreCase("save")) {
				if (plugin.hasPermission(sender, "commandsigns.save", false)) {
					plugin.saveFile();
					Messaging.sendMessage(sender, "success.saved");
				}
			} else if (args[0].equalsIgnoreCase("update")) {
				if (sender.hasPermission("commandsigns.update")) {
					if (plugin.version < plugin.newestVersion) {
						if (!plugin.getUpdateFile().exists()) {
							Messaging.sendMessage(sender, "update.start", "v", plugin.stringNew);
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
										Messaging.sendMessage(sender, "update.finish", "s", "" + (((totalBytesRead)) / 1000), "t", "" + (((double) (endTime - startTime)) / 1000));
										writer.close();
										reader.close();
									} catch (MalformedURLException e) {
										Messaging.sendMessage(sender, "update.fetch_error", "e", e.getMessage());
									} catch (IOException e) {
										Messaging.sendMessage(sender, "update.fetch_error", "e", e.getMessage());
									}
								}
							}.start();
						} else {
							Messaging.sendMessage(sender, "update.already_downloaded");
						}
					} else {
						Messaging.sendMessage(sender, "update.up_to_date", "v", plugin.getDescription().getVersion());
					}
				}
			} else {
				Messaging.sendMessage(sender, "failure.wrong_syntax");
			}
			return true;
		}
		return false;
	}
}
