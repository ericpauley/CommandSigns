package org.zonedabone.commandsigns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.zonedabone.commandsigns.Metrics.Graph;
import org.zonedabone.commandsigns.Metrics.Plotter;

public class CommandSigns extends JavaPlugin {
	
	public static Economy economy = null;
	public static Permission permission = null;
	public final Map<CommandSignsLocation, CommandSignsText> activeSigns = new HashMap<CommandSignsLocation, CommandSignsText>();
	private CommandSignsCommand commandExecutor = new CommandSignsCommand(this);
	// listeners
	private final CommandSignsEventListener listener = new CommandSignsEventListener(this);
	// plugin variables
	public final Map<String, CommandSignsPlayerState> playerStates = new HashMap<String, CommandSignsPlayerState>();
	public final Map<String, CommandSignsText> playerText = new HashMap<String, CommandSignsText>();
	public final Map<CommandSignsLocation, Map<String, Long>> timeouts = new ConcurrentHashMap<CommandSignsLocation, Map<String, Long>>();
	public final Set<String> running = Collections.synchronizedSet(new HashSet<String>());
	private Metrics metrics;
	
	public synchronized Map<String, Long> getSignTimeouts(CommandSignsLocation csl) {
		Map<String, Long> toReturn = timeouts.get(csl);
		if (toReturn == null) {
			toReturn = new ConcurrentHashMap<String, Long>();
			timeouts.put(csl, toReturn);
		}
		return toReturn;
	}
	
	public boolean hasPermission(Player player, String string) {
		return hasPermission(player, string, true);
	}
	
	public boolean hasPermission(Player player, String string, boolean notify) {
		boolean perm;
		if (permission == null) {
			perm = player.hasPermission(string);
		} else {
			perm = permission.has(player, string);
		}
		if (perm == false && notify) {
			player.sendMessage(ChatColor.RED + "You do not have permission.");
		}
		return perm;
	}
	
	public void startMetrics() {
		try {
			metrics = new Metrics(this);
		} catch (IOException e) {
			this.getLogger().warning("Could not initialize metrics.");
			e.printStackTrace();
			return;
		}
		Graph g = metrics.createGraph("Number of CommandSigns");
		g.addPlotter(new Plotter() {
			
			@Override
			public int getValue() {
				return activeSigns.size();
			}
		});
		if(metrics.start()){
			this.getLogger().info("Plugin metrics enabled! Thank you!");
		}else{
			this.getLogger().info("You opted out of CommandSigns metrics. =(");
		}
		
	}
	
	public void loadFile() {
		try {
			File file = new File(getDataFolder(), "signs.dat");
			if (file.exists()) {
				FileInputStream inStream = new FileInputStream(file);
				Scanner scanner = new Scanner(inStream);
				while (scanner.hasNextLine()) {
					try {
						String line = scanner.nextLine();
						if (!line.equals("")) {
							if (line.contains("\u00A7")) {
								String[] raw = line.split("\u00A7");
								String world = raw[0];
								int x = Integer.parseInt(raw[1]);
								int y = Integer.parseInt(raw[2]);
								int z = Integer.parseInt(raw[3]);
								CommandSignsLocation csl = new CommandSignsLocation(Bukkit.getWorld(world), x, y, z);
								String owner = raw[4];
								CommandSignsText cst = new CommandSignsText(owner);
								for (String command : raw[5].split("\u00B6")) {
									cst.getText().add(command);
								}
								activeSigns.put(csl, cst);
							} else {
								String[] data = line.split(":", 2);
								String[] extra = data[0].split("\\|");
								CommandSignsLocation location = CommandSignsLocation.fromFileString(extra[0]);
								if (location == null) {
									continue;
								}
								String[] textData = data[1].split("\\[LINEBREAK]");
								CommandSignsText text;
								if (extra.length >= 2) {
									text = new CommandSignsText(extra[1]);
								} else {
									text = new CommandSignsText(null);
								}
								for (int i = 0; i < textData.length; i++) {
									text.setLine(i, textData[i]);
								}
								activeSigns.put(location, text);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				scanner.close();
				inStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		saveFile();
	}
	
	@Override
	public void onEnable() {
		loadFile();
		PluginManager pm = getServer().getPluginManager();
		getCommand("commandsigns").setExecutor(commandExecutor);
		pm.registerEvents(listener, this);
		startMetrics();
		setupPermissions();
		setupEconomy();
	}
	
	public void saveFile() {
		try {
			File file = new File(getDataFolder(), "signs.dat");
			if (!file.exists()) {
				getDataFolder().mkdir();
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("");
			for (Map.Entry<CommandSignsLocation, CommandSignsText> entry : activeSigns.entrySet()) {
				String commands = "";
				entry.getValue().trim();
				for (String command : entry.getValue().getText()) {
					if (!commands.equals(""))
						commands += "\u00B6";
					commands += command;
				}
				CommandSignsLocation csl = entry.getKey();
				String sep = "\u00A7";
				String line = csl.getWorld().getName();
				line += sep;
				line += csl.getX();
				line += sep;
				line += csl.getY();
				line += sep;
				line += csl.getZ();
				line += sep;
				line += entry.getValue().getOwner();
				line += sep;
				line += commands;
				writer.write(line + "\n");
			}
			writer.close();
		} catch (IOException ex) {
			getLogger().severe("Failed to save signs!");
			ex.printStackTrace();
		}
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return permission != null;
	}
}