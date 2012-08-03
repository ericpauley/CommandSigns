package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
	
	private List<String> parseCommandSign(Player player, Location loc, CommandSignsText commandSign) {
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
		CommandSignsText text = plugin.activeSigns.get(location);
		if (text == null) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		CommandSignsText clone = plugin.activeSigns.get(location).clone(player.getName());
		plugin.playerText.put(player, clone);
		readSign();
		Messaging.sendMessage(player, "success.copied");
		plugin.playerStates.put(player, CommandSignsPlayerState.ENABLE);
	}
	
	public void disableSign() {
		if (!plugin.activeSigns.containsKey(location)) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		plugin.activeSigns.remove(location);
		if (plugin.playerText.containsKey(player)) {
			plugin.playerStates.put(player, CommandSignsPlayerState.ENABLE);
			player.sendMessage("Sign disabled. You still have text in your clipboard.");
		} else {
			plugin.playerStates.remove(player);
			player.sendMessage("Sign disabled.");
		}
	}
	
	public void enableSign() {
		if (plugin.activeSigns.containsKey(location)) {
			player.sendMessage("Sign is already enabled!");
			return;
		}
		CommandSignsText text = plugin.playerText.get(player);
		plugin.activeSigns.put(location, text.clone(player.getName()));
		plugin.playerStates.remove(player);
		plugin.playerText.remove(player);
		player.sendMessage("CommandSign enabled");
	}
	
	private boolean inGroup(String group) {
		boolean in = false;
		for (String s : CommandSigns.permission.getPlayerGroups(player)) {
			if (s.equalsIgnoreCase(group)) {
				in = true;
			}
		}
		/*
		 * in = in || plugin.hasPermission(player, "commandsigns.group." + group, false);
		 * in = in || plugin.hasPermission(player, "commandsigns.group.*", false);
		 */
		return in;
	}
	
	public void runSign(Action a) {
		final CommandSignsText cst = plugin.activeSigns.get(location);
		if (cst == null)
			return;
		System.out.println("TEST1");
		if (player == null || plugin.hasPermission(player, "commandsigns.use.regular")) {
			Future<List<String>> future = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<List<String>>() {
				
				@Override
				public List<String> call() throws Exception {
					return parseCommandSign(player, location, cst);
				}
			});
			List<String> commandList;
			try {
				commandList = future.get();
			} catch (InterruptedException e1) {
				return;
			} catch (ExecutionException e1) {
				return;
			}
			System.out.println("TEST2");
			if (player != null) {
				if (plugin.running.contains(player))
					return;
				plugin.running.add(player);
			}
			boolean groupFiltered = false;
			boolean moneyFiltered = false;
			boolean permFiltered = false;
			boolean timeFiltered = false;
			boolean usesFiltered = false;
			boolean setTime = true;
			boolean randFiltered = false;
			boolean clickFiltered = false;
			boolean elsed = false;
			for (String command : commandList) {
				boolean show = true;
				boolean global = false;
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
				} else if (command.equals("`")) {
					usesFiltered = false;
				} else if (command.equals("-")) {
					elsed = !elsed;
				} else if (command.equals("?")) {
					elsed = !elsed;
				} else if (command.equals("<")) {
					elsed = !elsed;
				}
				while (command.startsWith("#") || command.startsWith("!")) {
					if (command.startsWith("!")) {
						show = false;
						command = command.substring(1);
					}
					if (command.startsWith("#")) {
						global = true;
						command = command.substring(1);
					}
				}
				if (!elsed) {
					if (groupFiltered || moneyFiltered || permFiltered || timeFiltered || usesFiltered || randFiltered || clickFiltered) {
						continue;
					}
				} else {
					if (!(groupFiltered || moneyFiltered || permFiltered || timeFiltered || usesFiltered || randFiltered || clickFiltered)) {
						continue;
					}
				}
				if (player != null && !global && command.startsWith("~")) {
					int amount = 0;
					try {
						amount = (int) (Double.parseDouble(command.substring(1)) * 1000);
					} catch (NumberFormatException e) {
					}
					long lastUse = cst.getLastUse(player);
					if (lastUse + amount > System.currentTimeMillis()) {
						timeFiltered = true;
						setTime = false;
						if (show)
							player.sendMessage(ChatColor.RED + "You must wait another " + Math.round((amount + lastUse - System.currentTimeMillis()) / 1000 + 1) + " seconds before using this sign again.");
					} else if (lastUse != 0 && amount == 0) {
						timeFiltered = true;
						setTime = false;
						if (show)
							player.sendMessage(ChatColor.RED + "This sign can only be used once.");
					}
				} else if (command.startsWith("~")) {
					int amount = 0;
					try {
						amount = (int) (Double.parseDouble(command.substring(1)) * 1000);
					} catch (NumberFormatException e) {
					}
					long lastUse = cst.getLastUse();
					if (lastUse + amount > System.currentTimeMillis()) {
						timeFiltered = true;
						setTime = false;
						if (show && player != null)
							player.sendMessage(ChatColor.RED + "This sign will re-active in " + Math.round((amount + lastUse - System.currentTimeMillis()) / 1000 + 1) + " seconds.");
					} else if (lastUse != 0 && amount == 0) {
						timeFiltered = true;
						setTime = false;
						if (show && player != null)
							player.sendMessage(ChatColor.RED + "This sign can only be used once globally.");
					}
				} else if (player != null && !global && command.startsWith("`")) {
					int amount = 0;
					try {
						amount = (int) (Double.parseDouble(command.substring(1)));
					} catch (NumberFormatException e) {
					}
					if (cst.getUses(player) > amount) {
						usesFiltered = true;
						setTime = false;
						if (show && player != null)
							player.sendMessage(ChatColor.RED + "You have already used this sign the maximum number of times. (" + amount + ")");
					}
				} else if (command.startsWith("`")) {
					int amount = 0;
					try {
						amount = (int) (Double.parseDouble(command.substring(1)));
					} catch (NumberFormatException e) {
					}
					if (cst.getNumUses() > amount) {
						usesFiltered = true;
						setTime = false;
						if (show && player != null)
							player.sendMessage(ChatColor.RED + "This sign has already been used too many times. (" + amount + ")");
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
				} else if (command.startsWith("?")) {
					double amount = 0;
					try {
						amount = Double.parseDouble(command.substring(1));
					} catch (NumberFormatException e) {
					}
					randFiltered = Math.random() * 100 > amount;
				} else if (command.equals("<<")) {
					randFiltered = (a != Action.RIGHT_CLICK_BLOCK);
				} else if (player != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("&")) {
					permFiltered = !plugin.hasPermission(player, command.substring(1));
					if (permFiltered && show) {
						player.sendMessage(ChatColor.RED + "You don't have permission to use this CommandSign.");
					}
				} else if (player != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("@")) {
					groupFiltered = !inGroup(command.substring(1));
					System.out.println(groupFiltered);
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
			if (setTime) {
				cst.setLastUse(System.currentTimeMillis());
				cst.use();
			}
			if (player != null) {
				if (setTime) {
					cst.setLastUse(player);
					cst.use(player);
				}
				plugin.running.remove(player);
			}
		}
	}
	
	public void onInteract(Action action) {
		CommandSignsPlayerState state = plugin.playerStates.get(player);
		if (state != null) {
			switch (state) {
				case ENABLE :
					enableSign();
					break;
				case REMOVE :
					disableSign();
					break;
				case READ :
					readSign();
					break;
				case COPY :
					copySign();
					break;
				case EDIT_SELECT :
					editSign();
					break;
				default :
					Material m = location.getBlock().getType();
					if ((m == Material.WOOD_PLATE || m == Material.STONE_PLATE) && action == Action.RIGHT_CLICK_BLOCK)
						return;
					runSign(action);
					break;
			}
		} else {
			Material m = location.getBlock().getType();
			if ((m == Material.WOOD_PLATE || m == Material.STONE_PLATE) && action == Action.RIGHT_CLICK_BLOCK)
				return;
			runSign(action);
		}
	}
	
	public void editSign() {
		CommandSignsText cst = plugin.activeSigns.get(location);
		if (cst == null) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		Messaging.sendMessage(player, "progress.edit_started");
		plugin.playerText.put(player, cst);
		plugin.playerStates.put(player, CommandSignsPlayerState.EDIT);
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
		plugin.playerStates.remove(player);
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
							ConsoleCommandSender ccs;
							if (show)
								ccs = new CommandSignsProxy(plugin.getServer().getConsoleSender(), player);
							else
								ccs = new CommandSignsProxy(plugin.getServer().getConsoleSender(), null);
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
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
			}
		}
	}
}
