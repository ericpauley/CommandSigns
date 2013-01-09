package org.zonedabone.commandsigns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.zonedabone.commandsigns.listener.CommandListener;
import org.zonedabone.commandsigns.listener.EventListener;
import org.zonedabone.commandsigns.thirdparty.Metrics;
import org.zonedabone.commandsigns.thirdparty.Metrics.Graph;
import org.zonedabone.commandsigns.thirdparty.Metrics.Plotter;
import org.zonedabone.commandsigns.util.Config;
import org.zonedabone.commandsigns.util.Messaging;
import org.zonedabone.commandsigns.util.PlayerState;
import org.zonedabone.commandsigns.util.SignText;
import org.zonedabone.commandsigns.util.Updater;

public class CommandSigns extends JavaPlugin {

	// Listeners
	private final EventListener listener = new EventListener(this);
	public CommandListener commandExecutor = new CommandListener(this);

	// Third-party
	private Metrics metrics;

	public static Economy economy = null;
	public static Permission permission = null;

	// Plugin variables
	public final Map<Location, SignText> activeSigns = new HashMap<Location, SignText>();
	public final Map<OfflinePlayer, PlayerState> playerStates = new HashMap<OfflinePlayer, PlayerState>();
	public final Map<OfflinePlayer, SignText> playerText = new HashMap<OfflinePlayer, SignText>();

	public Config config = new Config(this);
	public Messaging messenger = new Messaging(this);
	public Updater updateHandler = new Updater(this);

	// Class variables
	private BukkitTask updateTask;

	public File getUpdateFile() {
		return new File(getServer().getUpdateFolderFile().getAbsoluteFile(),
				super.getFile().getName());
	}

	public boolean hasPermission(CommandSender player, String string) {
		return hasPermission(player, string, true);
	}

	public boolean hasPermission(CommandSender player, String string,
			boolean notify) {
		boolean perm;
		if (permission == null) {
			perm = player.hasPermission(string);
		} else {
			perm = permission.has(player, string);
		}
		if (perm == false && notify) {
			messenger.sendMessage(player, "failure.no_perms");
		}
		return perm;
	}

	public void loadFile() {
		activeSigns.clear();
		if (new File(getDataFolder(), "signs.dat").exists()) {
			loadOldFile();
			if (!new File(getDataFolder(), "signs.dat").exists()) {
				saveFile();
			}
			new File(getDataFolder(), "signs.dat").renameTo(new File(
					getDataFolder(), "signs.bak"));
		}
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(new File(getDataFolder(), "signs.yml"));
		ConfigurationSection data = config.getConfigurationSection("signs");
		if (data == null) {
			getLogger().info("No signs found.");
			return;
		}
		String[] locText;
		World world;
		int x, y, z, block;
		Location loc;
		int attempts = 0;
		for (String key : data.getKeys(false)) {
			try {
				attempts++;
				locText = key.split(",");
				world = Bukkit.getWorld(locText[0]);
				if (world == null)
					continue;
				x = Integer.parseInt(locText[1]);
				y = Integer.parseInt(locText[2]);
				z = Integer.parseInt(locText[3]);
				loc = new Location(world, x, y, z);

				// Throws exception for an invalid location AND if the
				// location is air
				block = loc.getBlock().getTypeId();
				if (block == 0)
					throw new IllegalArgumentException("Location not valid.");

				boolean redstone = data.getBoolean(key + ".redstone", false);
				String owner = data.getString(key + ".owner", null);
				SignText cst = new SignText(owner, redstone);
				for (Object o : data.getList(key + ".text",
						new ArrayList<String>())) {
					cst.addLine(o.toString());
				}
				cst.setEnabled(data.getBoolean(key + ".active", true));
				Map<String, Long> timeouts = cst.getTimeouts();
				ConfigurationSection cooldowns = data
						.getConfigurationSection(key + ".cooldowns");
				if (cooldowns == null) {
					cooldowns = data.createSection(key + "cooldowns");
				}
				for (String subKey : cooldowns.getKeys(false)) {
					timeouts.put(subKey, cooldowns.getLong(subKey));
				}
				/*
				 * cst.setLastUse(data.getLong(key + ".lastuse", 0));
				 * cst.setNumUses(data.getLong(key + ".numuses", 0)); for
				 * (Object useData : data.getList(key + ".usedata", new
				 * ArrayList<String>())) { String[] sections =
				 * useData.toString().split(","); OfflinePlayer user =
				 * Bukkit.getOfflinePlayer(sections[0]); long lastUse =
				 * Long.parseLong(sections[1]); long numUses =
				 * Long.parseLong(sections[2]); cst.getTimeouts().put(user,
				 * lastUse); cst.getUses().put(user, numUses); }
				 */
				activeSigns.put(loc, cst);
			} catch (Exception ex) {
				getLogger().warning(
						"Unable to load sign " + attempts + " in signs.yml. "
								+ ex.getMessage());
				ex.printStackTrace();
			}
		}
		getLogger().info(
				"Successfully loaded " + activeSigns.size() + " signs.");
	}

	public void loadOldFile() {
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

						// Throws exception for an invalid location AND if the
						// location is air
						block = csl.getBlock().getTypeId();
						if (block == 0)
							throw new IllegalArgumentException(
									"Location not valid.");

						String owner = raw[4];
						SignText cst = new SignText(owner, redstone);
						for (String command : raw[5].split("[\u00B6\u001E]")) {
							cst.addLine(command);
						}
						activeSigns.put(csl, cst);
					} catch (Exception ex) {
						getLogger().warning(
								"Unable to load sign in signs.dat line "
										+ lineNumber);
					}
				}
				scanner.close();
				inStream.close();
				getLogger().info(
						"Imported " + activeSigns.size() + " old signs");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		if (updateTask != null)
			updateTask.cancel();
		saveFile();
	}

	@Override
	public void onEnable() {
		load();
		PluginManager pm = getServer().getPluginManager();
		getCommand("commandsigns").setExecutor(commandExecutor);
		pm.registerEvents(listener, this);
	}

	public void load() {
		config.load();
		messenger.load();
		loadFile();
		setupPermissions();
		setupEconomy();

		if (config.getBoolean("updater.auto-check") == true)
			startUpdateCheck();

		if (config.getBoolean("metrics.enable") == true)
			startMetrics();
		else
			getLogger().info(messenger.parseRaw("metrics.opt_out"));
	}

	public void saveFile() {
		FileConfiguration config = new YamlConfiguration();
		ConfigurationSection data = config.createSection("signs");
		for (Map.Entry<Location, SignText> sign : activeSigns.entrySet()) {
			Location loc = sign.getKey();
			SignText cst = sign.getValue();
			cst.trim();
			String key = loc.getWorld().getName() + "," + loc.getBlockX() + ","
					+ loc.getBlockY() + "," + loc.getBlockZ();
			ConfigurationSection signData = data.createSection(key);
			signData.set("redstone", cst.isRedstone());
			signData.set("owner", cst.getOwner());
			signData.set("text", cst.getText());
			signData.set("active", cst.isEnabled());
			signData.createSection("cooldowns", cst.getTimeouts());
			/*
			 * data.set(key + ".lastuse", cst.getLastUse()); data.set(key +
			 * ".numuses", cst.getNumUses()); List<String> useData = new
			 * ArrayList<String>(cst.getUses().size()); for (OfflinePlayer user
			 * : cst.getTimeouts().keySet()) { useData.add(user.getName() + ","
			 * + cst.getLastUse(user) + "," + cst.getUses(user)); } data.set(key
			 * + ".usedata", useData);
			 */
			try {
				config.save(new File(getDataFolder(), "signs.yml"));
			} catch (IOException e) {
				getLogger().severe("Failed to save CommandSigns");
				e.printStackTrace();
			}
		}
	}

	public void saveOldFile() {
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
			for (Map.Entry<Location, SignText> entry : activeSigns.entrySet()) {
				try {
					signNumber++;
					entry.getValue().trim();
					commands = "";
					for (String command : entry.getValue()) {
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
					if (csl != null)
						getLogger().warning(
								"Unable to save sign #" + signNumber + " at "
										+ csl.toString());
					else
						getLogger().warning(
								"Unable to save sign #" + signNumber);
				}
			}
			writer.close();
		} catch (Exception ex) {
			getLogger().severe("Failed to save signs!");
			ex.printStackTrace();
		}
	}

	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}

	public boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return permission != null;
	}

	public void startMetrics() {
		try {
			metrics = new Metrics(this);
		} catch (IOException e) {
			getLogger().warning(messenger.parseRaw("metrics.failure"));
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
				for (SignText cst : activeSigns.values()) {
					for (String s : cst) {
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
				for (SignText cst : activeSigns.values()) {
					for (String s : cst) {
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
				for (SignText cst : activeSigns.values()) {
					for (String s : cst) {
						if (s.startsWith("/#") || s.startsWith("!/#")) {
							number++;
						}
					}
				}
				return number;
			}
		});
		if (metrics.start()) {
			getLogger().info(messenger.parseRaw("metrics.success"));
		} else {
			getLogger().info(messenger.parseRaw("metrics.failure"));
		}
	}

	public void startUpdateCheck() {
		Runnable checker = updateHandler.new Checker();
		updateTask = getServer().getScheduler().runTaskTimer(this, checker, 0,
				1728000L);
	}
}