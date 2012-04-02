package org.zonedabone.commandsigns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandSigns extends JavaPlugin {
	
	public static Economy economy = null;
	public static Permission permission = null;
	public final HashMap<CommandSignsLocation, CommandSignsText> activeSigns = new HashMap<CommandSignsLocation, CommandSignsText>();
	private CommandSignsCommand commandExecutor = new CommandSignsCommand(this);
	// listeners
	private final CommandSignsEventListener listener = new CommandSignsEventListener(this);
	// plugin variables
	public final HashMap<String, CommandSignsPlayerState> playerStates = new HashMap<String, CommandSignsPlayerState>();
	public final HashMap<String, CommandSignsText> playerText = new HashMap<String, CommandSignsText>();
	
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
	
	public void loadFile() {
		try {
			File file = new File(this.getDataFolder(), "signs.dat");
			if (file.exists()) {
				FileInputStream inStream = new FileInputStream(file);
				Scanner scanner = new Scanner(inStream);
				String line;
				String[] data;
				String[] textData;
				while (scanner.hasNextLine()) {
					try {
						line = scanner.nextLine();
						if (!line.equals("")) {
							data = line.split(":", 2);
							String[] extra = data[0].split("\\|");
							CommandSignsLocation location = CommandSignsLocation.fromFileString(extra[0]);
							if (location == null)
								continue;
							textData = data[1].split("\\[LINEBREAK]");
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
		setupPermissions();
		setupEconomy();
	}
	
	public void saveFile() {
		try {
			File file = new File(this.getDataFolder(), "signs.dat");
			if (!file.exists()) {
				this.getDataFolder().mkdir();
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("");
			for (Map.Entry<CommandSignsLocation, CommandSignsText> entry : activeSigns.entrySet()) {
				String keyString = entry.getKey().toFileString();
				CommandSignsText value = entry.getValue();
				String line = keyString + "|" + value.getOwner() + ":" + value.toFileString() + "\n";
				writer.write(line);
			}
			writer.close();
		} catch (IOException ex) {
			this.getLogger().severe("Failed to save signs!");
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