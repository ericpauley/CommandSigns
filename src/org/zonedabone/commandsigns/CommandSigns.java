package org.zonedabone.commandsigns;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.gravitydevelopment.updater.Updater;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.zonedabone.commandsigns.config.Config;
import org.zonedabone.commandsigns.config.Messaging;
import org.zonedabone.commandsigns.listener.CommandListener;
import org.zonedabone.commandsigns.listener.EventListener;
import org.zonedabone.commandsigns.util.PlayerState;
import org.zonedabone.commandsigns.util.SignLoader;
import org.zonedabone.commandsigns.util.SignText;

public class CommandSigns extends JavaPlugin {

	// Listeners
	private final EventListener listener = new EventListener(this);
	public CommandListener commandExecutor = new CommandListener(this);

	// Third-party
	private final int bukkitId = 37306;

	public static Economy economy = null;
	public static Permission permission = null;

	// Plugin variables
	public final Map<Location, SignText> activeSigns = new HashMap<Location, SignText>();
	public final Map<OfflinePlayer, PlayerState> playerStates = new HashMap<OfflinePlayer, PlayerState>();
	public final Map<OfflinePlayer, SignText> playerText = new HashMap<OfflinePlayer, SignText>();

	public SignLoader loader = new SignLoader(this);
	public Config config = new Config(this);
	public Messaging messenger = new Messaging(this);
    public Updater updater;

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

	public void load() {
		config.load();
		messenger.load();
		loader.loadFile();
		setupPermissions();
		setupEconomy();
		
		if (config.getBoolean("updater.auto-check") == true) {
            if (config.getBoolean("updater.auto-install") == true)
                updater = new Updater(this, getBukkitId(), this.getFile(), Updater.UpdateType.DEFAULT, false);
            else
                updater = new Updater(this, getBukkitId(), this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
        }
	}

	@Override
	public void onDisable() {
		loader.saveFile();
	}

	@Override
	public void onEnable() {
		load();
		PluginManager pm = this.getServer().getPluginManager();
		getCommand("commandsigns").setExecutor(commandExecutor);
		pm.registerEvents(listener, this);
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

    public int getBukkitId() {
        return bukkitId;
    }
    
    @Override
    public File getFile() {
        return super.getFile();
    }
}