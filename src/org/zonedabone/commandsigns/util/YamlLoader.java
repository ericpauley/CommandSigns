package org.zonedabone.commandsigns.util;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class YamlLoader {
    
    /**
     * Loads or updates a YAML configuration file
     * 
     * @param plugin
     * @param filename
     * @return
     */
    public static Configuration loadResource(JavaPlugin plugin, String filename) {
        File f = new File(plugin.getDataFolder(), filename);
        
        // Load the included file
        FileConfiguration internal = YamlConfiguration.loadConfiguration(plugin.getResource(filename));
        
        // Write the included file if an external one doesn't exist
        if (!f.exists()) {
            plugin.getLogger().info("Creating default " + filename + ".");
            plugin.saveResource(filename, true);
        }
        
        // Load the external file
        Configuration external = YamlConfiguration.loadConfiguration(f);
        
        // Check file for changes
        Set<String> internalKeys = internal.getKeys(true);
        Set<String> externalKeys = external.getKeys(true);
        
        if (!internalKeys.equals(externalKeys)) {
            plugin.getLogger().info("Updating " + filename + ".");
            
            // Copy all the loaded configuration into the new internal format
            for (String k : externalKeys) {
                if (external.isString(k) && internal.contains(k)) {
                    internal.set(k, external.getString(k));
                }
            }
            
            // Write the file to disk
            try {
                internal.save(f);
            } catch (IOException e) {
                plugin.getLogger().info("Could not update " + filename + ".");
            }
            
            // Copy the new configuration back into external
            external = internal;
        }
        
        return external;
    }
    
}
