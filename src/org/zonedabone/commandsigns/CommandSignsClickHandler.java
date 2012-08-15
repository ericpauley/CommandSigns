package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
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
		plugin.redstoneLock.remove(location);
		plugin.timeouts.remove(location);
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
		in = in || plugin.hasPermission(player, "commandsigns.group." + group, false);
		in = in || plugin.hasPermission(player, "commandsigns.group.*", false);
		return in;
	}
	
	public void runSign() {
		final CommandSignsText cst = plugin.activeSigns.get(location);
		if (cst == null)
			return;
		if (player == null || plugin.hasPermission(player, "commandsigns.use.regular")) {
			Future<List<String>> future = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<List<String>>() {
				
				@Override
				public List<String> call() throws Exception {
					return parseCommandSign(player, location, cst);
				}
			});
			List<String> commandList;
			while (true) {
				try {
					commandList = future.get();
					if (commandList != null) {
						break;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (player != null) {
				if (plugin.running.contains(player))
					return;
				plugin.running.add(player);
			} else {
				if (plugin.redstoneLock.contains(location))
					return;
				plugin.redstoneLock.add(location);
			}
			// This stack holds booleans of whether the current restriction block is enabled or denied
			Stack<Boolean> restrictions = new Stack<Boolean>();
			Map<OfflinePlayer, Long> lastUse = plugin.getSignTimeouts(location);
			for (String command : commandList) {
				if (command.equals(""))
					continue;
				
				boolean silent = false;
				boolean negate = false;
				boolean meta = false;
				do {
					meta = false;
					// The '-' delimiter ends the current restriction block
				 	if (command.startsWith("-") && !restrictions.isEmpty()) {
				 		restrictions.pop();
				 		command = command.substring(1);
				 		meta = true;
				 	}
				 	// If the restriction begins with a ?, make it silent
				 	else if (command.startsWith("?")) {
						silent = true;
						command = command.substring(1);
						meta = true;
					}
				 	// If the restriction starts with a !, negate the block
					else if (command.startsWith("!")) {
						negate = true;
						command = command.substring(1);
						meta = true;
					}
				} while(meta == true);
				
				// If a restriction block is denied, skip to next line
				if (!restrictions.isEmpty() && restrictions.peek().equals(false))
					continue;

				if (command.equals(""))
					continue;

				if (player != null && command.startsWith("~")) {
					int amount = 0;
					try {
						amount = (int) (Double.parseDouble(command.substring(1)) * 1000);
					} catch (NumberFormatException e) {
					}
					Long latest = lastUse.get(player);
					// If condition is true and negate is true, reject
					// If condition is false and negate is false, reject
					// If condition is true and negate is false or vice versa, accept (XOR)
					if ((latest == null || System.currentTimeMillis() - latest > amount) ^ negate) {
						lastUse.put(player, System.currentTimeMillis());
						// Set the current command block to be enabled
						restrictions.push(true);
					} else {
						// Set the current command block to be denied
						restrictions.push(false);
						// Show error if not silent
						if (!silent) {
							if (negate)
								player.sendMessage(ChatColor.RED + "You must click again within " + amount / 1000 + " seconds to use this sign.");
							else
								player.sendMessage(ChatColor.RED + "You must wait another " + Math.round((amount + latest - System.currentTimeMillis()) / 1000 + 1) + " seconds before using this sign again.");
						}
						if (negate) {
							lastUse.put(player, System.currentTimeMillis());
							latest = lastUse.get(player);
						}
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
					if (plugin.hasPermission(player, command.substring(1)) ^ negate) {
						restrictions.push(true);
					} else {
						restrictions.push(false);
						if (!silent)
							player.sendMessage(ChatColor.RED + "You don't have permission to use this CommandSign.");
					}
				} else if (player != null && CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("@")) {
					if (inGroup(command.substring(1)) ^ negate) {
						restrictions.push(true);
					} else {
						restrictions.push(false);
						if (!silent)
							player.sendMessage(ChatColor.RED + "You are not in the required group to run this command.");
					}
				} else if (player != null && CommandSigns.economy != null && CommandSigns.economy.isEnabled() && command.startsWith("$")) {
					double amount = 0;
					try {
						amount = Double.parseDouble(command.substring(1));
					} catch (NumberFormatException e) {
					}
					if (CommandSigns.economy.withdrawPlayer(player.getName(), amount).transactionSuccess() ^ negate) {
						restrictions.push(true);
					} else {
						restrictions.push(false);
						if (!silent)
							player.sendMessage(ChatColor.RED + "You cannot afford to use this CommandSign. (" + CommandSigns.economy.format(amount) + ")");
					}
				} else if (command.startsWith("/") || command.startsWith("\\")) {
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RunHandler(command, silent));
				}
			}
			if (player != null) {
				plugin.running.remove(player);
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
		CommandSignsPlayerState state = plugin.playerStates.get(player);
		if (state != null && action == Action.RIGHT_CLICK_BLOCK) {
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
					runSign();
					break;
			}
		} else {
			Material m = location.getBlock().getType();
			if ((m == Material.WOOD_PLATE || m == Material.STONE_PLATE) && action == Action.RIGHT_CLICK_BLOCK)
				return;
			runSign();
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
		
		private String command;
		private boolean silent;
		
		public RunHandler(String command, boolean silent) {
			this.silent = silent;
			this.command = command;
		}
		
		@Override
		public void run() {
			boolean op = false;
			List<PermissionAttachment> given = new ArrayList<PermissionAttachment>();
			List<String> vGiven = new ArrayList<String>();
			if(command.startsWith("\\")) {
				command = command.substring(1);
				player.sendMessage(command.replaceAll("&([0-9A-FK-OR])", "\u00A7$1"));
			} else if(command.startsWith("/")) {
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
