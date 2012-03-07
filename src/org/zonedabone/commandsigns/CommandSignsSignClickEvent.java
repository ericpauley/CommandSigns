package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.PermissionAttachment;

public class CommandSignsSignClickEvent {

	private static CommandSigns plugin;

	// private static String[] delimiters = {"/","\\\\","@"};

	public CommandSignsSignClickEvent(CommandSigns instance) {
		plugin = instance;
	}

	public void onRightClick(PlayerInteractEvent event, Sign sign) {
		Player player = event.getPlayer();
		CommandSignsLocation location = new CommandSignsLocation(sign.getX(),
				sign.getY(), sign.getZ());
		CommandSignsPlayerState state = plugin.playerStates.get(player
				.getName());
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
			if(!importSign(player,location)){
				return;
			}
			
		}
		List<String> commandList = parseCommandSign(player,
				plugin.activeSigns.get(location));
		if (plugin.hasPermission(player, "commandsigns.use.regular")) {
			String groupFilter = null;

			for (String command : commandList) {
				if (command.indexOf("@") != 0 && groupFilter != null
						&& !inGroup(player, groupFilter)) {
					continue;
				}

				if (command.startsWith("@")) {
					if (command.length() <= 1) {
						groupFilter = null;
					} else {
						groupFilter = command.substring(1).trim();
					}
					continue;
				}

				if (command.startsWith("/")) {
					PermissionAttachment newPermission = null;
					if (command.length() <= 1) {
						player.sendMessage("Error, SignCommand /command is of length 0.");
						continue;
					}
					if (command.indexOf("*") == 1) {
						command = command.substring(1);
						if (plugin.hasPermission(player,
								"commandsigns.use.super")) {
							if (!plugin.hasPermission(player,
									"commandsigns.permissions")) {
								newPermission = player.addAttachment(plugin,
										"commandsigns.permissions", true);
							}
						} else {
							player.sendMessage("You may not use this type of sign.");
							continue;
						}
					}
					player.performCommand(command.substring(1));
					if (newPermission != null) {
						newPermission.remove();
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

	private boolean inGroup(Player player, String group) {
		String permissionsNode = ("commandsigns.group." + group);
		if (plugin.hasPermission(player, permissionsNode)) {
			return true;
		} else if (plugin.hasPermission(player, "commandsigns.group.*")) {

		}
		player.sendMessage("You do not have that group permission");
		return false;
	}

	public static List<String> parseCommandSign(Player player,
			CommandSignsText commandSign) {
		List<String> commandList = new ArrayList<String>();
		String line;
		for (int i = 0; i < 10; i++) {
			line = commandSign.getLine(i);
			if (line != null) {
				line = line.replace("<X>", ""
						+ player.getLocation().getBlockX());
				line = line.replace("<Y>", ""
						+ player.getLocation().getBlockY());
				line = line.replace("<Z>", ""
						+ player.getLocation().getBlockZ());
				line = line.replace("<NAME>", "" + player.getName());
				commandList.add(line);
			}
		}
		return commandList;
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
	
	public boolean importSign(Player player, CommandSignsLocation l){
		if(!plugin.hasPermission(player,"commandsigns.import"))return false;
		Sign s = (Sign) player.getWorld().getBlockAt(l.getX(),l.getY(), l.getZ()).getState();
		String[] lines = s.getLines();
		if(lines[0].equalsIgnoreCase("[command]")||lines[0].equalsIgnoreCase("[scs]")){
			CommandSignsText cst = new CommandSignsText();
			cst.setLine(1, lines[1]+" "+lines[2]+" "+lines[3]);
			plugin.activeSigns.put(l,cst);
			player.sendMessage("Just importing that sign now...");
			return true;
		}else{
			return false;
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

	public void copySign(Player player, CommandSignsLocation location) {
		String playerName = player.getName();
		CommandSignsText text = plugin.activeSigns.get(location);
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
}
