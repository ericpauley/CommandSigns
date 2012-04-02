package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.PermissionAttachment;

public class CommandSignsSignClickEvent {
	
	private static CommandSigns plugin;
	
	// private static String[] delimiters = {"/","\\\\","@"};
	public static List<String> parseCommandSign(Player player, CommandSignsText commandSign) {
		List<String> commandList = new ArrayList<String>();
		String line;
		for (int i = 0; i < 10; i++) {
			line = commandSign.getLine(i);
			if (line != null) {
				line = line.replace("<X>", "" + player.getLocation().getBlockX());
				line = line.replace("<Y>", "" + player.getLocation().getBlockY());
				line = line.replace("<Z>", "" + player.getLocation().getBlockZ());
				line = line.replace("<NAME>", "" + player.getName());
				commandList.add(line);
			}
		}
		return commandList;
	}
	
	public CommandSignsSignClickEvent(CommandSigns instance) {
		plugin = instance;
	}
	
	public void copySign(Player player, CommandSignsLocation location) {
		String playerName = player.getName();
		CommandSignsText text = plugin.activeSigns.get(location).clone(player.getName());
		if (text == null) {
			player.sendMessage("Sign is not a CommandSign.");
		}
		plugin.playerText.put(playerName, text);
		String[] lines = text.getText();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] != null) {
				player.sendMessage("Line" + i + ": " + lines[i]);
			}
		}
		player.sendMessage("Added to CommandSigns clipboard. Click a sign to enable.");
		plugin.playerStates.put(playerName, CommandSignsPlayerState.ENABLE);
	}
	
	public void disableSign(Player player, CommandSignsLocation location) {
		String playerName = player.getName();
		if (!plugin.activeSigns.containsKey(location)) {
			player.sendMessage("Sign is not enabled!");
			plugin.playerStates.remove(playerName);
			return;
		}
		plugin.activeSigns.remove(location);
		if (plugin.playerText.containsKey(playerName)) {
			plugin.playerStates.put(playerName, CommandSignsPlayerState.ENABLE);
			player.sendMessage("Sign disabled. You still have text in your clipboard.");
		} else {
			plugin.playerStates.remove(playerName);
			player.sendMessage("Sign disabled.");
		}
	}
	
	// To parse a sign... kept in case regular sign support is added again
	/*
	 * public static List<String> parseSignText(Player player, String text) {
	 * text = text.replace("<X>", ""+ player.getLocation().getBlockX()); text =
	 * text.replace("<Y>", ""+ player.getLocation().getBlockY()); text =
	 * text.replace("<Z>", ""+ player.getLocation().getBlockZ()); text =
	 * text.replace("<NAME>", ""+ player.getName()); List<String> commandList =
	 * new ArrayList<String>(); commandList.add(text); for(String delimiter :
	 * delimiters) { List<String> commandSplit = new ArrayList<String>();
	 * for(String s : commandList) { String[] split = s.split(delimiter);
	 * for(int i=0;i<split.length;i++) { if(split[i].length()>1)
	 * commandSplit.add((i!=0?delimiter:"")+split[i]); } } commandList =
	 * commandSplit; } return commandList; }
	 */
	public void enableSign(Player player, CommandSignsLocation location) {
		if (plugin.activeSigns.containsKey(location)) {
			player.sendMessage("Sign is already enabled!");
			return;
		}
		CommandSignsText text = plugin.playerText.get(player.getName());
		plugin.activeSigns.put(location, text);
		plugin.playerStates.remove(player.getName());
		plugin.playerText.remove(player.getName());
		player.sendMessage("CommandSign enabled");
	}
	
	public boolean importSign(Player player, CommandSignsLocation l, Sign s) {
		if (!plugin.hasPermission(player, "commandsigns.import")) {
			return false;
		}
		String[] lines = s.getLines();
		if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[command]") || ChatColor.stripColor(lines[0]).equalsIgnoreCase("[scs]")) {
			CommandSignsText cst = new CommandSignsText(null);
			cst.setLine(1, ChatColor.stripColor(lines[1]) + " " + ChatColor.stripColor(lines[2]) + " " + ChatColor.stripColor(lines[3]).trim());
			if (cst.getLine(1).startsWith("/*") || cst.getLine(1).startsWith("/^") || cst.getLine(1).startsWith("/#")) {
				if (!plugin.hasPermission(player, "commandsigns.create.super", false)) {
					player.sendMessage("You cannot import super signs.");
					return false;
				}
			}
			plugin.activeSigns.put(l, cst);
			player.sendMessage("Just importing that sign now...");
			return true;
		} else {
			return false;
		}
	}
	
	private boolean inGroup(Player player, String group) {
		boolean in = false;
		for (String s : CommandSigns.permission.getPlayerGroups(player)) {
			if (s.equalsIgnoreCase(group)) {
				in = true;
			}
		}
		if (in) {
			return true;
		} else {
			player.sendMessage(ChatColor.RED+"You are not in the rquired group to run this command.");
			return false;
		}
	}
	
	public void onRightClick(PlayerInteractEvent event, Sign sign) {
		Player player = event.getPlayer();
		CommandSignsLocation location = new CommandSignsLocation(sign.getX(), sign.getY(), sign.getZ(), sign.getWorld());
		CommandSignsPlayerState state = plugin.playerStates.get(player.getName());
		if (state != null) {
			if (state.equals(CommandSignsPlayerState.ENABLE)) {
				enableSign(player, location);
			} else if (state.equals(CommandSignsPlayerState.DISABLE)) {
				disableSign(player, location);
			} else if (state.equals(CommandSignsPlayerState.READ)) {
				readSign(player, location);
			} else if (state.equals(CommandSignsPlayerState.COPY)) {
				copySign(player, location);
			}
			return;
		}
		if (!plugin.activeSigns.containsKey(location)) {
			if (!importSign(player, location, sign)) {
				return;
			}
		}
		List<String> commandList = parseCommandSign(player, plugin.activeSigns.get(location));
		if (plugin.hasPermission(player, "commandsigns.use.regular")) {
			boolean groupFiltered = false;
			boolean moneyFiltered = false;
			for (String command : commandList) {
				if (CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("@")) {
					if (command.equals("@")) {
						groupFiltered = false;
					} else {
						groupFiltered = !inGroup(player, command.substring(1));
					}
					continue;
				}
				if (CommandSigns.economy != null && CommandSigns.economy.isEnabled() && command.startsWith("$")) {
					if (command.equals("$")) {
						moneyFiltered = false;
					} else {
						double amount = 0;
						try {
							amount = Double.parseDouble(command.substring(1));
						} catch (NumberFormatException e) {
						}
						moneyFiltered = !CommandSigns.economy.withdrawPlayer(player.getName(), amount).transactionSuccess();
						if(moneyFiltered){
							player.sendMessage(ChatColor.RED+"You cannot afford to use this CommandSign. ("+CommandSigns.economy.format(amount)+")");
						}
					}
				}
				if (groupFiltered || moneyFiltered) {
					continue;
				}
				if (command.startsWith("/")) {
					boolean op = false;
					PermissionAttachment perm = null;
					if (command.length() <= 1) {
						player.sendMessage("Error, SignCommand /command is of length 0.");
						continue;
					}
					try {
						if (command.startsWith("/*")) {
							command = command.substring(1);
							if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
								/*for (Map.Entry<String, Boolean> s : Bukkit.getPluginManager().getPermission("commandsigns.permissions").getChildren().entrySet()) {
								}*/
								perm = player.addAttachment(plugin, "commandsigns.permissions", true);
								player.performCommand(command.substring(1));
							} else {
								player.sendMessage("You may not use this type of sign.");
								continue;
							}
						} else if (command.startsWith("/^")) {
							command = command.substring(1);
							if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
								if (!player.isOp()) {
									op = true;
									player.setOp(true);
								}
								player.performCommand(command.substring(1));
							} else {
								player.sendMessage("You may not use this type of sign.");
								continue;
							}
						} else if (command.startsWith("/#")) {
							command = command.substring(1);
							if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
								plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.substring(1));
							} else {
								player.sendMessage("You may not use this type of sign.");
								continue;
							}
						} else {
							player.performCommand(command.substring(1));
						}
					} finally {
						if (perm != null) {
							perm.remove();
						}
						if (op) {
							player.setOp(false);
						}
					}
					continue;
				}
				if (command.startsWith("\\")) {
					String msg = command.substring(1);
					player.sendMessage(msg);
					continue;
				}
			}
		}
	}
	
	public void readSign(Player player, CommandSignsLocation location) {
		CommandSignsText text = plugin.activeSigns.get(location);
		if (text == null) {
			player.sendMessage("Sign is not a CommandSign.");
		}
		String[] lines = text.getText();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] != null) {
				player.sendMessage("Line" + i + ": " + lines[i]);
			}
		}
		plugin.playerStates.remove(player.getName());
	}
}
