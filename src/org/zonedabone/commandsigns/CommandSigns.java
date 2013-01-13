package org.zonedabone.commandsigns;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.zonedabone.commandsigns.config.Config;
import org.zonedabone.commandsigns.config.Messaging;
import org.zonedabone.commandsigns.listener.CommandListener;
import org.zonedabone.commandsigns.listener.EventListener;
import org.zonedabone.commandsigns.thirdparty.Metrics;
import org.zonedabone.commandsigns.thirdparty.Metrics.Graph;
import org.zonedabone.commandsigns.thirdparty.Metrics.Plotter;
import org.zonedabone.commandsigns.util.PlayerState;
import org.zonedabone.commandsigns.util.SignLoader;
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

	public SignLoader loader = new SignLoader(this);
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

	@Override
	public void onDisable() {
		if (updateTask != null)
			updateTask.cancel();
		loader.saveFile();
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
		loader.loadFile();
		setupPermissions();
		setupEconomy();

		if (config.getBoolean("updater.auto-check") == true)
			startUpdateCheck();

		if (config.getBoolean("metrics.enable") == true)
			startMetrics();
		else
			getLogger().info(messenger.parseRaw("metrics.opt_out"));
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