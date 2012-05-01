package org.zonedabone.commandsigns;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.zonedabone.commandsigns.Metrics.Graph;
import org.zonedabone.commandsigns.Metrics.Plotter;

public class CommandSigns extends JavaPlugin {
	
	public static Economy economy = null;
	public static Permission permission = null;
	public final Map<Location, CommandSignsText> activeSigns = new HashMap<Location, CommandSignsText>();
	private CommandSignsCommand commandExecutor = new CommandSignsCommand(this);
	// listeners
	private final CommandSignsEventListener listener = new CommandSignsEventListener(this);
	// plugin variables
	public final Map<OfflinePlayer, CommandSignsPlayerState> playerStates = new HashMap<OfflinePlayer, CommandSignsPlayerState>();
	public final Map<OfflinePlayer, CommandSignsText> playerText = new HashMap<OfflinePlayer, CommandSignsText>();
	public final Map<Location, Map<OfflinePlayer, Long>> timeouts = new ConcurrentHashMap<Location, Map<OfflinePlayer, Long>>();
	public final Set<OfflinePlayer> running = Collections.synchronizedSet(new HashSet<OfflinePlayer>());
	public final Set<Location> redstoneLock = Collections.synchronizedSet(new HashSet<Location>());
	private Metrics metrics;
	public int version, newestVersion;
	public String downloadLocation, stringNew;
	private int updateTask;
	
	public synchronized Map<OfflinePlayer, Long> getSignTimeouts(Location csl) {
		Map<OfflinePlayer, Long> toReturn = timeouts.get(csl);
		if (toReturn == null) {
			toReturn = new ConcurrentHashMap<OfflinePlayer, Long>();
			timeouts.put(csl, toReturn);
		}
		return toReturn;
	}
	
	public boolean hasPermission(CommandSender player, String string) {
		return hasPermission(player, string, true);
	}
	
	public boolean hasPermission(CommandSender player, String string, boolean notify) {
		boolean perm;
		if (permission == null) {
			perm = player.hasPermission(string);
		} else {
			perm = permission.has(player, string);
		}
		if (perm == false && notify) {
			Messaging.sendMessage(player, "failure.no_perms");
		}
		return perm;
	}
	
	public void startMetrics() {
		try {
			metrics = new Metrics(this);
		} catch (IOException e) {
			getLogger().warning(Messaging.parseRaw("metrics.failure"));
		}
		Graph g = metrics.createGraph("Number of CommandSigns");
		g.addPlotter(new Plotter() {
			
			@Override
			public int getValue() {
				return activeSigns.size();
			}
		});
		Graph g3 = metrics.createGraph("CommandSigns Version");
		g3.addPlotter(new Plotter(getDescription().getVersion()) {
			
			@Override
			public int getValue() {
				return 1;
			}
		});
		Graph g2 = metrics.createGraph("Super Signs Used");
		g2.addPlotter(new Plotter("Permission") {
			
			@Override
			public int getValue() {
				int number = 0;
				for (CommandSignsText cst : activeSigns.values()) {
					for (String s : cst.getText()) {
						if (s.startsWith("/*") || s.startsWith("!/*")) {
							number++;
						}
					}
				}
				return number;
			}
		});
		g2.addPlotter(new Plotter("Op") {
			
			@Override
			public int getValue() {
				int number = 0;
				for (CommandSignsText cst : activeSigns.values()) {
					for (String s : cst.getText()) {
						if (s.startsWith("/^") || s.startsWith("!/^")) {
							number++;
						}
					}
				}
				return number;
			}
		});
		g2.addPlotter(new Plotter("Console") {
			
			@Override
			public int getValue() {
				int number = 0;
				for (CommandSignsText cst : activeSigns.values()) {
					for (String s : cst.getText()) {
						if (s.startsWith("/#") || s.startsWith("!/#")) {
							number++;
						}
					}
				}
				return number;
			}
		});
		if (metrics.start()) {
			getLogger().info(Messaging.parseRaw("metrics.success"));
		} else {
			getLogger().info(Messaging.parseRaw("metrics.optout"));
		}
	}
	
	public void loadFile() {
		try {
			File file = new File(getDataFolder(), "signs.dat");
			if (file.exists()) {
				FileInputStream inStream = new FileInputStream(file);
				Scanner scanner = new Scanner(inStream);
				int loaded = 0;
				while (scanner.hasNextLine()) {
					try {
						String line = scanner.nextLine();
						if (!line.equals("")) {
							// if (line.contains("\u00A7")) {
							String[] raw = line.split("\u00A7");
							String world = raw[0];
							int x = Integer.parseInt(raw[1]);
							int y = Integer.parseInt(raw[2]);
							int z = Integer.parseInt(raw[3]);
							Location csl = new Location(Bukkit.getWorld(world), x, y, z);
							String owner = raw[4];
							boolean redstone = false;
							if (raw.length >= 7)
								redstone = Boolean.parseBoolean(raw[6]);
							CommandSignsText cst = new CommandSignsText(owner, redstone);
							for (String command : raw[5].split("\u00B6")) {
								cst.getText().add(command);
							}
							activeSigns.put(csl, cst);
							loaded++;
							/*
							 * } else {
							 * String[] data = line.split(":", 2);
							 * String[] extra = data[0].split("\\|");
							 * CommandSignsLocation location = CommandSignsLocation.fromFileString(extra[0]);
							 * if (location == null) {
							 * continue;
							 * }
							 * String[] textData = data[1].split("\\[LINEBREAK]");
							 * CommandSignsText text;
							 * if (extra.length >= 2) {
							 * text = new CommandSignsText(extra[1], false);
							 * } else {
							 * text = new CommandSignsText(null, false);
							 * }
							 * for (int i = 0; i < textData.length; i++) {
							 * text.setLine(i, textData[i]);
							 * }
							 * activeSigns.put(location, text);
							 * }
							 */
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				scanner.close();
				inStream.close();
				getLogger().info("Loaded " + loaded + " signs");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(updateTask);
		saveFile();
	}
	
	@Override
	public void onEnable() {
		Messaging.loadMessages(this);
		loadFile();
		PluginManager pm = getServer().getPluginManager();
		getCommand("commandsigns").setExecutor(commandExecutor);
		pm.registerEvents(listener, this);
		startUpdateCheck();
		startMetrics();
		setupPermissions();
		setupEconomy();
	}
	
	public void startUpdateCheck() {
		version = Integer.parseInt(getDescription().getVersion().replaceAll("\\.", ""));
		newestVersion = version;
		updateTask = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			
			@Override
			public void run() {
				try {
					// open HTTP connection
					URL url = new URL("http://cloud.github.com/downloads/zonedabone/CommandSigns/version.txt");
					URLConnection connection = url.openConnection();
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					// just read first line
					stringNew = in.readLine();
					newestVersion = Integer.parseInt(stringNew.replaceAll("\\.", ""));
					downloadLocation = in.readLine();
					in.close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
				}
			}
		}, 0, 24000);
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
			for (Map.Entry<Location, CommandSignsText> entry : activeSigns.entrySet()) {
				String commands = "";
				entry.getValue().trim();
				for (String command : entry.getValue().getText()) {
					if (!commands.equals(""))
						commands += "\u00B6";
					commands += command;
				}
				Location csl = entry.getKey();
				String sep = "\u00A7";
				String line = csl.getWorld().getName();
				line += sep;
				line += csl.getBlockX();
				line += sep;
				line += csl.getBlockY();
				line += sep;
				line += csl.getBlockZ();
				line += sep;
				line += entry.getValue().getOwner();
				line += sep;
				line += commands;
				line += sep;
				line += entry.getValue().isRedstone();
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
	
	public File getUpdateFile() {
		return new File(getServer().getUpdateFolderFile().getAbsoluteFile(), super.getFile().getName());
	}
}