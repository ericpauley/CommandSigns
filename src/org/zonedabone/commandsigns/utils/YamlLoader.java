package org.zonedabone.commandsigns.utils;

import java.io.File;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.utils.Updater.Version;

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
		Configuration included = YamlConfiguration.loadConfiguration(plugin
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
		// TODO: Update rather than rewrite
		if (incVersion.compareTo(extVersion) > 0) {
			plugin.getLogger().info("Updating " + filename + ".");
			plugin.saveResource(filename, true);
			external = YamlConfiguration.loadConfiguration(f);
		}
		
		return external;
	}
	
}
