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
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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
	public CommandSignsCommand commandExecutor = new CommandSignsCommand(this);
	public CommandSignsUpdater updateHandler = new CommandSignsUpdater(this);
	private int updateTask;
	// listeners
	private final CommandSignsEventListener listener = new CommandSignsEventListener(this);
	// plugin variables
	public final Map<OfflinePlayer, CommandSignsPlayerState> playerStates = new HashMap<OfflinePlayer, CommandSignsPlayerState>();
	public final Map<OfflinePlayer, CommandSignsText> playerText = new HashMap<OfflinePlayer, CommandSignsText>();
	public final Map<Location, Map<OfflinePlayer, Long>> timeouts = new ConcurrentHashMap<Location, Map<OfflinePlayer, Long>>();
	public final Set<OfflinePlayer> running = Collections.synchronizedSet(new HashSet<OfflinePlayer>());
	public final Set<Location> redstoneLock = Collections.synchronizedSet(new HashSet<Location>());
	private Metrics metrics;
	
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
				activeSigns.clear();
				
				String line = "";
				String[] raw = null;
				boolean redstone = false;
				World world = null;
				int x = 0;
				int y = 0;
				int z = 0;
				int block = 0;
				int lineNumber = 0;
				
				while (scanner.hasNextLine()) {
					lineNumber++;
					try {
						line = scanner.nextLine();
						raw = line.split("[\u00A7\u001D]");
						
						redstone = Boolean.parseBoolean(raw[6]);
						
						world = Bukkit.getWorld(raw[0]);
						x = Integer.parseInt(raw[1]);
						y = Integer.parseInt(raw[2]);
						z = Integer.parseInt(raw[3]);
						Location csl = new Location(world, x, y, z);
						
						// Throws exception for an invalid location AND if the location is air
						block = csl.getBlock().getTypeId();
						if(block == 0) throw new IllegalArgumentException("Location not valid.");
						
						String owner = raw[4];
						CommandSignsText cst = new CommandSignsText(owner, redstone);
						for (String command : raw[5].split("[\u00B6\u001E]")) {
							cst.getText().add(command);
						}
						activeSigns.put(csl, cst);
					} catch (Exception ex) {
						getLogger().warning("Unable to load sign in signs.dat line " + lineNumber);
					}
				}
				scanner.close();
				inStream.close();
				getLogger().info("Loaded " + activeSigns.size() + " signs");
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
		Runnable checker = updateHandler.new Checker();
		updateTask = getServer().getScheduler().scheduleAsyncRepeatingTask(this, checker, 0, 1728000L);
	}
	
	public void saveFile() {
		try {
			File file = new File(getDataFolder(), "signs.dat");
			if (!file.exists()) {
				getDataFolder().mkdir();
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			Location csl = null;
			String sep = "\u001D";
			String line = "";
			String commands = "";
			boolean first = true;
			int signNumber = 0;
			
			writer.write("");
			for (Map.Entry<Location, CommandSignsText> entry : activeSigns.entrySet()) {
				try {
					signNumber++;
					entry.getValue().trim();
					commands = "";
					for (String command : entry.getValue().getText()) {
						if (!first)
							commands += "\u001E";
						commands += command;
						first = false;
					}
					csl = entry.getKey();
					line = csl.getWorld().getName();
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
				} catch (Exception ex) {
					if(csl != null)
						getLogger().warning("Unable to save sign #" + signNumber + " at " + csl.toString());
					else
						getLogger().warning("Unable to save sign #" + signNumber);
				}
			}
			writer.close();
		} catch (Exception ex) {
			getLogger().severe("Failed to save signs!");
			ex.printStackTrace();
		}
	}
	
	protected boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}
	
	protected boolean setupPermissions() {
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