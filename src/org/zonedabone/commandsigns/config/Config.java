package org.zonedabone.commandsigns.config;

import org.bukkit.configuration.Configuration;
import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.util.YamlLoader;

public class Config extends ConfigStore {

	public Config(CommandSigns plugin) {
		super(plugin);
	}

	public void load() {
		Configuration config = YamlLoader.loadResource(plugin, "config.yml");

		for (String k : config.getKeys(true)) {
			if (!config.isConfigurationSection(k)) {
				this.put(k, config.getString(k));
			}
		}
	}

}
