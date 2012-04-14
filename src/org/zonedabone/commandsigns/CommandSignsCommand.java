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
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("commandsigns")) {
			if (args.length < 1) {
				return false;
			}
			final Player player = (Player) sender;
			String playerName = player.getName();
			if (args[0].indexOf("line") == 0) {
				if (plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super")) {
					int lineNumber;
					try {
						lineNumber = Integer.parseInt(args[0].substring(4));
					} catch (NumberFormatException ex) {
						MessageManager.sendMessage(player, "failure.invalid_line");
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
						MessageManager.sendMessage(player, "failure.no_super");
						return true;
					}
					text.setLine(lineNumber, line);
					text.trim();
					String display = line.replace("$", "\\$");
					MessageManager.sendRaw(player, "success.line_print", "n", "" + lineNumber, "l", display);
					plugin.playerStates.put(playerName, CommandSignsPlayerState.ENABLE);
					MessageManager.sendMessage(player, "progress.add");
				}
			} else if (args[0].equalsIgnoreCase("read")) {
				if (plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super")) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.READ);
					MessageManager.sendMessage(player, "progress.read");
				}
			} else if (args[0].equalsIgnoreCase("copy")) {
				if (plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super")) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.COPY);
					MessageManager.sendMessage(player, "progress.copy");
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (plugin.hasPermission(player, "commandsigns.remove")) {
					plugin.playerStates.put(playerName, CommandSignsPlayerState.DISABLE);
					MessageManager.sendMessage(player, "progress.remove");
				}
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (plugin.hasPermission(player, "commandsigns.remove", false) || plugin.hasPermission(player, "commandsigns.create.regular", false) || plugin.hasPermission(player, "commandsigns.create.super")) {
					plugin.playerStates.remove(playerName);
					plugin.playerText.remove(playerName);
					MessageManager.sendMessage(player, "success.cleared");
				}
			} else if (args[0].equalsIgnoreCase("update")) {
				if (player.hasPermission("commandsigns.update")) {
					if (plugin.version < plugin.newestVersion) {
						if (!plugin.getUpdateFile().exists()) {
							MessageManager.sendMessage(player, "update.start", "v", plugin.stringNew);
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
										MessageManager.sendMessage(player, "update.finish", "s", "" + (((totalBytesRead)) / 1000), "t", "" + (((double) (endTime - startTime)) / 1000));
										writer.close();
										reader.close();
									} catch (MalformedURLException e) {
										MessageManager.sendMessage(player, "update.fetch_error", "e", e.getMessage());
									} catch (IOException e) {
										MessageManager.sendMessage(player, "update.fetch_error", "e", e.getMessage());
									}
								}
							}.start();
						} else {
							MessageManager.sendMessage(player, "update.already_downloaded");
						}
					} else {
						MessageManager.sendMessage(player, "update.up_to_date", "v", plugin.getDescription().getVersion());
					}
				}
			} else {
				MessageManager.sendMessage(player, "failure.wrong_syntax");
			}
			return true;
		}
		return false;
	}
}
