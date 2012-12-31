package org.zonedabone.commandsigns.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.util.Updater.Version;

public class YamlLoader {
	
	/**
	 * Loads or updates a YAML configuration file
	 * 
	 * @param plugin
	 * @param filename
	 * @return 
	 */
	public static Configuration loadResource(CommandSigns plugin, String filename) {
		File f = new File(plugin.getDataFolder(), filename);
		
		// Load the included file
		FileConfiguration included = YamlConfiguration.loadConfiguration(plugin
				.getResource(filename));
		
		// Write the included file if an external one doens't exist
		if (!f.exists()) {
			plugin.getLogger().info("Creating default " + filename + ".");
			plugin.saveResource(filename, true);
		}

		// Load the external file
		Configuration external = YamlConfiguration.loadConfiguration(f);
		
		// Check version information
		Updater updaterClass = new Updater(plugin);
		Version extVersion = updaterClass.new Version(
				external.getString("config-version"));
		Version incVersion = updaterClass.new Version(
				included.getString("config-version"));

		// Update external file if included file is newer
		if (incVersion.compareTo(extVersion) > 0) {
			plugin.getLogger().info("Updating " + filename + ".");
			
			// Copy all the loaded configuration into the new included format
			for (String k : external.getKeys(true)) {
				if (external.isString(k) && included.contains(k)) {
					included.set(k, external.getString(k));
				}
			}
			
			// Write the file to disk
			try {
				included.save(f);
			} catch (IOException e) {
				plugin.getLogger().info("Could not update " + filename + ".");
			}
			
			// Copy the new configuration back into external
			external = included;
		}
		
		return external;
	}
	
}
