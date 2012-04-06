package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
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
		for (String line : commandSign.getText()) {
			line = line.replaceAll("(?iu)<x>", "" + player.getLocation().getBlockX());
			line = line.replaceAll("(?iu)<y>", "" + player.getLocation().getBlockY());
			line = line.replaceAll("(?iu)<z>", "" + player.getLocation().getBlockZ());
			line = line.replaceAll("(?iu)<world>", player.getWorld().getName());
			line = line.replace("(?iu)<name>", "" + player.getName());
			line = line.replace("(?iu)<display>", player.getDisplayName());
			if (CommandSigns.economy != null && CommandSigns.economy.isEnabled()) {
				line = line.replace("(?iu)<money>", "" + CommandSigns.economy.getBalance(player.getName()));
				line = line.replace("(?iu)<formatted>", CommandSigns.economy.format(CommandSigns.economy.getBalance(player.getName())));
			}
			commandList.add(line);
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
		this.readSign(player, location);
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
	
	public void enableSign(Player player, CommandSignsLocation location) {
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
		in = in || plugin.hasPermission(player, "commandsigns.group." + group, false);
		in = in || plugin.hasPermission(player, "commandsigns.group.*", false);
		return in;
	}
	
	public void onRightClick(PlayerInteractEvent event, Sign sign) {
		final Player player = event.getPlayer();
		CommandSignsLocation location = new CommandSignsLocation(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
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
			if (plugin.running.contains(player.getName()))
				return;
			plugin.running.add(player.getName());
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
				if(!elsed){
					if (groupFiltered || moneyFiltered || permFiltered || timeFiltered) {
						continue;
					}
				}else{
					if (!(groupFiltered || moneyFiltered || permFiltered || timeFiltered)) {
						continue;
					}
				}
				
				if (command.startsWith("~")) {
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
				}else if (command.startsWith("%")) {
					double amount = 0;
					try {
						amount = Double.parseDouble(command.substring(1));
					} catch (NumberFormatException e) {
					}
					try {
						Thread.sleep((long) (amount * 1000));
					} catch (InterruptedException e) {
					}
				}else if (CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("&")) {
					permFiltered = !plugin.hasPermission(player, command.substring(1));
					if (permFiltered && show) {
						player.sendMessage(ChatColor.RED + "You don't have permission to use this CommandSign.");
					}
				}else if (CommandSigns.permission != null && CommandSigns.permission.isEnabled() && command.startsWith("@")) {
					groupFiltered = !inGroup(player, command.substring(1));
					if (groupFiltered && show)
						player.sendMessage(ChatColor.RED + "You are not in the rquired group to run this command.");
				}else if (CommandSigns.economy != null && CommandSigns.economy.isEnabled() && command.startsWith("$")) {
					double amount = 0;
					try {
						amount = Double.parseDouble(command.substring(1));
					} catch (NumberFormatException e) {
					}
					moneyFiltered = !CommandSigns.economy.withdrawPlayer(player.getName(), amount).transactionSuccess();
					if (moneyFiltered && show) {
						player.sendMessage(ChatColor.RED + "You cannot afford to use this CommandSign. (" + CommandSigns.economy.format(amount) + ")");
					}
				}else if (command.startsWith("/")) {
					final boolean fShow = show;
					final String fCommand = command;
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
						
						private boolean show = fShow;
						private String command = fCommand;
						
						@Override
						public void run() {
							boolean op = false;
							List<PermissionAttachment> given = new ArrayList<PermissionAttachment>();
							List<String> vGiven = new ArrayList<String>();
							if (command.length() <= 1) {
								if (show)
									player.sendMessage("Error, SignCommand /command is of length 0.");
								return;
							}
							try {
								if (command.startsWith("/*")) {
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
										player.performCommand(command.substring(1));
									} else {
										if (show)
											player.sendMessage("You may not use this type of sign.");
										return;
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
										if (show)
											player.sendMessage("You may not use this type of sign.");
										return;
									}
								} else if (command.startsWith("/#")) {
									command = command.substring(1);
									if (plugin.hasPermission(player, "commandsigns.use.super", false)) {
										plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.substring(1));
									} else {
										if (show)
											player.sendMessage("You may not use this type of sign.");
										return;
									}
								} else {
									player.performCommand(command.substring(1));
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
						}
						
					});
					
				}else if (command.startsWith("\\")) {
					String msg = command.substring(1);
					if (show)
						player.sendMessage(msg.replaceAll("&([0-9A-FK-OR])", "\u00A7$1"));
					continue;
				}
			}
			if (setTime)
				lastUse.put(player.getName(), System.currentTimeMillis());
			plugin.running.remove(player.getName());
		}
	}
	
	public void readSign(Player player, CommandSignsLocation location) {
		CommandSignsText text = plugin.activeSigns.get(location);
		if (text == null) {
			player.sendMessage("Sign is not a CommandSign.");
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
}
