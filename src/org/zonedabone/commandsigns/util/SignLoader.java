package org.zonedabone.commandsigns.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.zonedabone.commandsigns.CommandSigns;

public class SignLoader {

	private CommandSigns plugin;

	public SignLoader(CommandSigns plugin) {
		this.plugin = plugin;
	}

	public synchronized void loadFile() {
		plugin.activeSigns.clear();
		
		if (new File(plugin.getDataFolder(), "signs.dat").exists()) {
			loadOldFile();
			if (!new File(plugin.getDataFolder(), "signs.dat").exists()) {
				saveFile();
			}
			new File(plugin.getDataFolder(), "signs.dat").renameTo(new File(
					plugin.getDataFolder(), "signs.bak"));
		}
		
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder(), "signs.yml"));
		ConfigurationSection data = config.getConfigurationSection("signs");
		
		if (data == null) {
			plugin.getLogger().info("No signs found.");
			return;
		}
		
		String[] locText;
		World world;
		int x, y, z;
        Material block;
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
				block = loc.getBlock().getType();
				if (block == null || block == Material.AIR)
					throw new IllegalArgumentException("Location not valid: "
							+ loc.toString() + ".");

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
				
				plugin.activeSigns.put(loc, cst);
			} catch (Exception ex) {
				plugin.getLogger().warning(
						"Unable to load sign " + attempts + " in signs.yml. "
								+ ex.getMessage());
				ex.printStackTrace();
			}
		}
		plugin.getLogger().info(
				"Successfully loaded " + plugin.activeSigns.size() + " signs");
	}

	public synchronized void loadOldFile() {
		try {
			File file = new File(plugin.getDataFolder(), "signs.dat");
			if (file.exists()) {
				FileInputStream inStream = new FileInputStream(file);
				Scanner scanner = new Scanner(inStream);
				plugin.activeSigns.clear();

				String line = "";
				String[] raw = null;
				boolean redstone = false;
				World world = null;
				int x = 0;
				int y = 0;
				int z = 0;
				Material block;
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
						block = csl.getBlock().getType();
						if (block == null || block == Material.AIR)
							throw new IllegalArgumentException(
									"Location not valid.");

						String owner = raw[4];
						SignText cst = new SignText(owner, redstone);
						for (String command : raw[5].split("[\u00B6\u001E]")) {
							cst.addLine(command);
						}
						plugin.activeSigns.put(csl, cst);
					} catch (Exception ex) {
						plugin.getLogger().warning(
								"Unable to load sign in signs.dat line "
										+ lineNumber);
					}
				}
				scanner.close();
				inStream.close();
				plugin.getLogger().info(
						"Imported " + plugin.activeSigns.size() + " old signs");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void saveFile() {
		FileConfiguration config = new YamlConfiguration();
		ConfigurationSection data = config.createSection("signs");
		
		for (Map.Entry<Location, SignText> sign : plugin.activeSigns.entrySet()) {
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
			
			try {
				config.save(new File(plugin.getDataFolder(), "signs.yml"));
			} catch (IOException e) {
				plugin.getLogger().severe("Failed to save CommandSigns");
				e.printStackTrace();
			}
		}
		plugin.getLogger().info(plugin.activeSigns.size()
                + " signs saved");
	}

	public synchronized void saveOldFile() {
		try {
			File file = new File(plugin.getDataFolder(), "signs.dat");
			if (!file.exists()) {
				plugin.getDataFolder().mkdir();
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
			for (Map.Entry<Location, SignText> entry : plugin.activeSigns
					.entrySet()) {
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
						plugin.getLogger().warning(
								"Unable to save sign #" + signNumber + " at "
										+ csl.toString());
					else
						plugin.getLogger().warning(
								"Unable to save sign #" + signNumber);
				}
			}
			writer.close();
		} catch (Exception ex) {
			plugin.getLogger().severe("Failed to save signs!");
			ex.printStackTrace();
		}
	}
}
