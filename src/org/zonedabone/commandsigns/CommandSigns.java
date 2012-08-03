package org.zonedabone.commandsigns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
	// listeners
	private final CommandSignsEventListener listener = new CommandSignsEventListener(this);
	// plugin variables
	public final Map<OfflinePlayer, CommandSignsPlayerState> playerStates = new HashMap<OfflinePlayer, CommandSignsPlayerState>();
	public final Map<OfflinePlayer, CommandSignsText> playerText = new HashMap<OfflinePlayer, CommandSignsText>();
	public final Set<OfflinePlayer> running = Collections.synchronizedSet(new HashSet<OfflinePlayer>());
	private Metrics metrics;
	public int version = 3;
	public int newestVersion;
	public String downloadLocation, stringNew;
	private int updateTask;
	private Importer importer;
	
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
		activeSigns.clear();
		if (new File(getDataFolder(), "signs.dat").exists()) {
			loadOldFile();
			if (!new File(getDataFolder(), "signs.dat").exists()) {
				saveFile();
			}
			new File(getDataFolder(), "signs.dat").renameTo(new File(getDataFolder(), "signs.bak"));
		}
		Configuration data = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "signs.yml"));
		for (String key : data.getKeys(false)) {
			String[] locText = key.split(",");
			World world = Bukkit.getWorld(locText[0]);
			if (world == null)
				continue;
			int x = Integer.parseInt(locText[1]);
			int y = Integer.parseInt(locText[2]);
			int z = Integer.parseInt(locText[3]);
			Location loc = new Location(world, x, y, z);
			boolean redstone = data.getBoolean(key + ".redstone", false);
			String owner = data.getString(key + ".owner", null);
			CommandSignsText cst = new CommandSignsText(owner, redstone);
			for (Object o : data.getList(key + ".text", new ArrayList<String>())) {
				cst.getText().add(o.toString());
			}
			cst.setLastUse(data.getLong(key + ".lastuse", 0));
			cst.setNumUses(data.getLong(key + ".numuses", 0));
			for (Object useData : data.getList(key + ".usedata", new ArrayList<String>())) {
				String[] sections = useData.toString().split(",");
				OfflinePlayer user = Bukkit.getOfflinePlayer(sections[0]);
				long lastUse = Long.parseLong(sections[1]);
				long numUses = Long.parseLong(sections[2]);
				cst.getTimeouts().put(user, lastUse);
				cst.getUses().put(user, numUses);
			}
			activeSigns.put(loc, cst);
		}
	}
	
	public void loadOldFile() {
		try {
			File file = new File(getDataFolder(), "signs.dat");
			if (file.exists()) {
				FileInputStream inStream = new FileInputStream(file);
				Scanner scanner = new Scanner(inStream);
				int loaded = 0;
				while (scanner.hasNextLine()) {
					try {
						String line = scanner.nextLine();
						line = line.replace('\u00A7', '\u001D');
						line = line.replace('\u00B6', '\u001E');
						if (!line.equals("")) {
							// if (line.contains("\u00A7")) {
							String[] raw = line.split("\u001D");
							World world = Bukkit.getWorld(raw[0]);
							if (world == null)
								continue;
							int x = Integer.parseInt(raw[1]);
							int y = Integer.parseInt(raw[2]);
							int z = Integer.parseInt(raw[3]);
							Location csl = new Location(world, x, y, z);
							String owner = raw[4];
							boolean redstone = false;
							long lastUse = 0;
							long uses = 0;
							if (raw.length >= 7)
								redstone = Boolean.parseBoolean(raw[6]);
							if (raw.length >= 8)
								lastUse = Long.parseLong(raw[7]);
							if (raw.length >= 10)
								uses = Long.parseLong(raw[9]);
							CommandSignsText cst = new CommandSignsText(owner, redstone);
							cst.setLastUse(lastUse);
							cst.setNumUses(uses);
							for (String command : raw[5].split("\u001E")) {
								cst.getText().add(command);
							}
							if (raw.length >= 9) {
								for (String timeouts : raw[8].split("\u001E")) {
									String[] split = timeouts.split(":");
									cst.getTimeouts().put(getServer().getOfflinePlayer(split[0]), Long.parseLong(split[1]));
									cst.getUses().put(getServer().getOfflinePlayer(split[0]), Long.parseLong(split[2]));
								}
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
		saveFile();
		getServer().getScheduler().cancelTask(updateTask);
	}
	
	@Override
	public void onEnable() {
		Messaging.loadMessages(this);
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
				loadFile();
			}
		});
		PluginManager pm = getServer().getPluginManager();
		getCommand("commandsigns").setExecutor(commandExecutor);
		pm.registerEvents(listener, this);
		/*
		 * importer = new Importer(this);
		 * importer.getDepends();
		 * importer.importData();
		 */
		startUpdateCheck();
		startMetrics();
		if (!setupPermissions()) {
			getLogger().warning("Could not hook Vault Permissions. There may be compatibility problems with PEX.");
		}
		if (!setupEconomy()) {
			getLogger().warning("Could not hook Vault Economy. Economy support disabled.");
		}
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			@Override
			public void run() {
				for (CommandSignsText cst : activeSigns.values()) {
					cst.setUsed(false);
				}
			}
		}, 0, 1);
	}
	
	public void startUpdateCheck() {
		newestVersion = version;
		updateTask = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Updater(), 0, 24000);
	}
	public class Updater implements Runnable {
		
		@Override
		public void run() {
			try {
				// open HTTP connection
				URL url = new URL("http://dl.dropbox.com/u/38069635/CommandSigns/version.txt");
				URLConnection connection = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				// just read first line
				stringNew = in.readLine();
				newestVersion = Integer.parseInt(in.readLine());
				downloadLocation = in.readLine();
				in.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
		}
	}
	
	public void saveFile() {
		long t = System.currentTimeMillis();
		FileConfiguration data = new YamlConfiguration();
		for (Map.Entry<Location, CommandSignsText> sign : activeSigns.entrySet()) {
			Location loc = sign.getKey();
			CommandSignsText cst = sign.getValue();
			String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
			data.set(key + ".redstone", cst.isRedstone());
			data.set(key + ".owner", cst.getOwner());
			data.set(key + ".text", cst.getText());
			data.set(key + ".lastuse", cst.getLastUse());
			data.set(key + ".numuses", cst.getNumUses());
			List<String> useData = new ArrayList<String>(cst.getUses().size());
			for (OfflinePlayer user : cst.getTimeouts().keySet()) {
				useData.add(user.getName() + "," + cst.getLastUse(user) + "," + cst.getUses(user));
			}
			data.set(key + ".usedata", useData);
			try {
				data.save(new File(getDataFolder(), "signs.yml"));
			} catch (IOException e) {
				getLogger().severe("Failed to save CommandSigns");
				e.printStackTrace();
			}
		}
		System.out.println(System.currentTimeMillis() - t);
	}
	
	public void saveOldFile() {
		try {
			File file = new File(getDataFolder(), "signs.dat");
			if (!file.exists()) {
				getDataFolder().mkdir();
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			writer.print("");
			for (Map.Entry<Location, CommandSignsText> entry : activeSigns.entrySet()) {
				String commands = "";
				entry.getValue().trim();
				boolean first = true;
				for (String command : entry.getValue().getText()) {
					if (!first)
						commands += "\u001E";
					commands += command;
					first = false;
				}
				String sessions = "";
				first = true;
				for (Map.Entry<OfflinePlayer, Long> timeout : entry.getValue().getTimeouts().entrySet()) {
					if (!first)
						sessions += "\u001E";
					sessions += timeout.getKey().getName() + ":" + timeout.getValue() + ":" + entry.getValue().getUses(timeout.getKey());
					first = false;
				}
				Location csl = entry.getKey();
				String sep = "\u001D";
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
				line += sep;
				line += entry.getValue().getLastUse();
				line += sep;
				line += sessions;
				line += sep;
				line += entry.getValue().getNumUses();
				writer.println(line + "\n");
			}
			writer.close();
		} catch (IOException ex) {
			getLogger().severe("Failed to save signs!");
			ex.printStackTrace();
		}
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null)
			return false;
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}
	
	private boolean setupPermissions() {
		if (getServer().getPluginManager().getPlugin("Vault") == null)
			return false;
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