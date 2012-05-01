package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.permissions.PermissionAttachment;

public class CommandSignsClickHandler {
	
	private CommandSigns plugin;
	private Player player;
	private Location location;
	
	public List<String> parseCommandSign(Player player, Location loc, CommandSignsText commandSign) {
		List<String> commandList = new ArrayList<String>();
		for (String line : commandSign.getText()) {
			line = line.replaceAll("(?iu)<blockx>", "" + loc.getX());
			line = line.replaceAll("(?iu)<blocky>", "" + loc.getY());
			line = line.replaceAll("(?iu)<blockz>", "" + loc.getZ());
			line = line.replaceAll("(?iu)<world>", loc.getWorld().getName());
			Player clp = null;
			int dist = Integer.MAX_VALUE;
			for (Player p : loc.getWorld().getPlayers()) {
				if (p.getLocation().distanceSquared(loc) < dist) {
					clp = p;
				}
			}
			if (clp != null) {
				line = line.replaceAll("(?iu)<near>", clp.getName());
			}
			if (player != null) {
				line = line.replaceAll("(?iu)<x>", "" + player.getLocation().getBlockX());
				line = line.replaceAll("(?iu)<y>", "" + player.getLocation().getBlockY());
				line = line.replaceAll("(?iu)<z>", "" + player.getLocation().getBlockZ());
				line = line.replaceAll("(?iu)<name>", "" + player.getName());
				line = line.replaceAll("(?iu)<display>", player.getDisplayName());
				if (CommandSigns.economy != null && CommandSigns.economy.isEnabled()) {
					line = line.replaceAll("(?iu)<money>", "" + CommandSigns.economy.getBalance(player.getName()));
					line = line.replaceAll("(?iu)<formatted>", CommandSigns.economy.format(CommandSigns.economy.getBalance(player.getName())));
				}
			}
			commandList.add(line);
		}
		return commandList;
	}
	
	public CommandSignsClickHandler(CommandSigns plugin, Player player, Block block) {
		this.plugin = plugin;
		this.player = player;
		location = block.getLocation();
	}
	
	public void copySign() {
		String playerName = player.getName();
		CommandSignsText text = plugin.activeSigns.get(location).clone(player.getName());
		if (text == null) {
			Messaging.sendMessage(player, "failure.not_a_sign");
		}
		plugin.playerText.put(playerName, text);
		readSign();
		Messaging.sendMessage(player, "success.copied");
		plugin.playerStates.put(playerName, CommandSignsPlayerState.ENABLE);
	}
	
	public void disableSign() {
		String playerName = player.getName();
		if (!plugin.activeSigns.containsKey(location)) {
			Messaging.sendMessage(player, "failure.not_a_sign");
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
	
	public void enableSign() {
		if (plugin.activeSigns.containsKey(location)) {
			player.sendMessage("Sign is already enabled!");
			return;
		}
		CommandSignsText text = plugin.playerText.get(player.getName());
		plugin.activeSigns.put(location, text.clone(player.getName()));
		plugin.playerStates.remove(player.getName());
		plugin.playerText.remove(player.getName());
		player.sendMessage("CommandSign enabled");
	}
	
	private boolean inGroup(String group) {
		boolean in = false;
		for (String s : CommandSigns.permission.getPlayerGroups(player)) {
			if (s.equalsIgnoreCase(group)) {
				in = true;
			}
		}
		in = in || plugin.hasPermission(player, "commandsigns.group." + group, false);
		in = in || plugin.hasPermission(player, "commandsigns.group.*", false);
		return in;
	}
	
	public void runSign() {
		CommandSignsText cst = plugin.activeSigns.get(location);
		if (cst == null)
			return;
		if (player == null || plugin.hasPermission(player, "commandsigns.use.regular")) {
			List<String> commandList = parseCommandSign(player, location, cst);
			if (player != null) {
				if (plugin.running.contains(player.getName()))
					return;
				plugin.running.add(player.getName());
			} else {
				if (plugin.redstoneLock.contains(location))
					return;
				plugin.redstoneLock.add(location);
			}
			boolean groupFiltered = false;
			boolean moneyFiltered = false;
			boolean permFiltered = false;
			boolean timeFiltered = false;
			boolean setTime = true;
			boolean elsed = false;
			Map<String, Long> lastUse = plugin.getSignTimeouts(location);
			for (String command : commandList) {
				boolean show = true;
				if (command.equals(""))
					continue;
				if (command.equals("@")) {
					groupFiltered = false;
				} else if (command.equals("$")) {
					moneyFiltered = false;
				} else if (command.equals("&")) {
					permFiltered = false;
				} else if (command.equals("~")) {
					timeFiltered = false;
				} else if (command.startsWith("!")) {
					show = false;
					command = command.substring(1);
				} else if (command.equals("-")) {
					elsed = !elsed;
				}
				if (!elsed) {
					if (groupFiltered || moneyFiltered || permFiltered || timeFiltered) {
						continue;
					}
				} else {
					if (!(groupFiltered || moneyFiltered || permFiltered || timeFiltered)) {
						continue;
					}
				}
				if (player != null && command.startsWith("~")) {
					int amount = 0;
					try {
						amount = (int) (Double.parseDouble(command.substring(1)) * 1000);
					} catch (NumberFormatException e) {
					}
					Long latest = lastUse.get(player.getName());
					if (latest != null && latest + amount > System.currentTimeMillis()) {
						timeFiltered = true;
						setTime = false;
						if (show)
							player.sendMessage(ChatColor.RED + "You must wait another " + Math.round((amount + latest - System.currentTimeMillis()) / 1000 + 1) + " seconds before using this sign again.");
					}
				} else if (command.startsWith("%")) {
					double amount = 0;
					try {
						amount = Double.parseDouble(command.substring(1));
					} catch (NumberFormatException e) {
					}
					try {
						Thread.sleep((long) (amount * 1000));
					} catch (InterruptedException e) {
					}
				} else if (player != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("&")) {
					permFiltered = !plugin.hasPermission(player, command.substring(1));
					if (permFiltered && show) {
						player.sendMessage(ChatColor.RED + "You don't have permission to use this CommandSign.");
					}
				} else if (player != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("@")) {
					groupFiltered = !inGroup(command.substring(1));
					if (groupFiltered && show)
						player.sendMessage(ChatColor.RED + "You are not in the rquired group to run this command.");
				} else if (player != null && CommandSigns.economy != null && CommandSigns.economy.isEnabled() && command.startsWith("$")) {
					double amount = 0;
					try {
						amount = Double.parseDouble(command.substring(1));
					} catch (NumberFormatException e) {
					}
					moneyFiltered = !CommandSigns.economy.withdrawPlayer(player.getName(), amount).transactionSuccess();
					if (moneyFiltered && show) {
						player.sendMessage(ChatColor.RED + "You cannot afford to use this CommandSign. (" + CommandSigns.economy.format(amount) + ")");
					}
				} else if (command.startsWith("/")) {
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RunHandler(show, command));
				} else if (command.startsWith("\\")) {
					String msg = command.substring(1);
					if (show)
						player.sendMessage(msg.replaceAll("&([0-9A-FK-OR])", "\u00A7$1"));
					continue;
				}
			}
			if (player != null) {
				if (setTime)
					lastUse.put(player.getName(), System.currentTimeMillis());
				plugin.running.remove(player.getName());
			} else {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					
					@Override
					public void run() {
						plugin.redstoneLock.remove(location);
					}
				});
			}
		}
	}
	
	public void onInteract(Action action) {
		CommandSignsPlayerState state = plugin.playerStates.get(player.getName());
		if (state != null && action == Action.RIGHT_CLICK_BLOCK) {
			switch (state) {
				case ENABLE :
					enableSign();
					break;
				case DISABLE :
					disableSign();
					break;
				case READ :
					readSign();
					break;
				case COPY :
					copySign();
					break;
			}
		} else {
			Material m = location.getBlock().getType();
			if ((m == Material.WOOD_PLATE || m == Material.STONE_PLATE) && action == Action.RIGHT_CLICK_BLOCK)
				return;
			runSign();
		}
	}
	
	public void readSign() {
		CommandSignsText text = plugin.activeSigns.get(location);
		if (text == null) {
			player.sendMessage("Sign is not a CommandSign.");
			return;
		}
		int i = 0;
		for (String s : text.getText()) {
			if (!s.equals("")) {
				player.sendMessage(i + ": " + s);
			}
			i++;
		}
		plugin.playerStates.remove(player.getName());
	}
	private class RunHandler implements Runnable {
		
		private boolean show;
		private String command;
		
		public RunHandler(boolean show, String command) {
			this.show = show;
			this.command = command;
		}
		
		@Override
		public void run() {
			boolean op = false;
			List<PermissionAttachment> given = new ArrayList<PermissionAttachment>();
			List<String> vGiven = new ArrayList<String>();
			command = command.substring(1);
			if (command.length() == 0) {
				return;
			}
			if (player != null) {
				try {
					if (command.startsWith("*")) {
						command = command.substring(1);
						if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
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
						} else {
							if (show)
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
						} else {
							if (show)
								Messaging.sendMessage(player, "cannot_use");
							return;
						}
					} else if (command.startsWith("#")) {
						command = command.substring(1);
						if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
							ConsoleCommandSender ccs = new CommandSignsProxy(plugin.getServer().getConsoleSender(), player);
							plugin.getServer().dispatchCommand(ccs, command);
						} else {
							if (show)
								Messaging.sendMessage(player, "cannot_use");
							return;
						}
					} else {
						player.performCommand(command);
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
				ConsoleCommandSender ccs = new CommandSignsProxy(plugin.getServer().getConsoleSender(), plugin.getServer().getConsoleSender());
				plugin.getServer().dispatchCommand(ccs, command);
			}
		}
	}
}
