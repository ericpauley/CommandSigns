package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.permissions.PermissionAttachment;

public class CommandSignExecutor {

	private CommandSigns plugin;
	private Player player;
	private Location location;
	private LinkedList<String> lines;
	private Stack<Boolean> restrictions = new Stack<Boolean>();
	private Map<OfflinePlayer, Long> lastUse;
	private Action action;

	public CommandSignExecutor(CommandSigns plugin, Player player, Location location, Action action) {
		this.plugin = plugin;
		this.player = player;
		this.action = action;
		this.location = location;
		lastUse = plugin.getSignTimeouts(location);
		if (player == null
				|| plugin.hasPermission(player, "commandsigns.use.regular")) {
			lines = parseCommandSign(player, location);
		} else {
			lines = new LinkedList<String>();
		}
		runLines();
	}

	private void runLines() {
		double wait = 0;
		while (wait == 0 && !lines.isEmpty()) {
			String command = lines.poll();
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
			
			// If an empty line is negated, invert the top of the stack. (For else)
			if (command.equals("")){
				if(negate && !restrictions.isEmpty())
					restrictions.push(!restrictions.pop());
				continue;
			}
				
			// If a restriction block is denied, skip to next line
			if (!restrictions.isEmpty() && restrictions.peek().equals(false))
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
				wait = amount;
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
			} else if (player != null && command.startsWith(">>")) {
				if (action == Action.RIGHT_CLICK_BLOCK) {
					restrictions.push(true);
				} else {
					restrictions.push(false);
				}
			} else if (command.startsWith("/") || command.startsWith("\\")) {
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
		if(wait!=0){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

				@Override
				public void run() {
					runLines();
				}
				
			}, (long) (wait*20));
		}
	}

	private LinkedList<String> parseCommandSign(Player player, Location loc) {
		LinkedList<String> commandList = new LinkedList<String>();
		CommandSignsText commandSign = plugin.activeSigns.get(location);
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
				line = line.replaceAll("(?iu)<x>", ""
						+ player.getLocation().getBlockX());
				line = line.replaceAll("(?iu)<y>", ""
						+ player.getLocation().getBlockY());
				line = line.replaceAll("(?iu)<z>", ""
						+ player.getLocation().getBlockZ());
				line = line.replaceAll("(?iu)<name>", "" + player.getName());
				line = line.replaceAll("(?iu)<display>",
						player.getDisplayName());
				if (CommandSigns.economy != null
						&& CommandSigns.economy.isEnabled()) {
					line = line
							.replaceAll(
									"(?iu)<money>",
									""
											+ CommandSigns.economy
													.getBalance(player
															.getName()));
					line = line.replaceAll("(?iu)<formatted>",
							CommandSigns.economy.format(CommandSigns.economy
									.getBalance(player.getName())));
				}
			}
			commandList.add(line);
		}
		return commandList;
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

}
