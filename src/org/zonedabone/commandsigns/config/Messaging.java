package org.zonedabone.commandsigns.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.util.YamlLoader;

public class Messaging extends ConfigStore {

	private static Map<String, String> messages = new ConcurrentHashMap<String, String>();

	public Messaging(CommandSigns plugin) {
		super(plugin);
	}

	/**
	 * {@inheritDoc}
	 */
	public void load() {
		Configuration config = YamlLoader.loadResource(plugin, "messages.yml");

		for (String k : config.getKeys(true)) {
			if (!config.isConfigurationSection(k)) {
				messages.put(k, config.getString(k));
			}
		}
	}

	public String parseMessage(String messageName) {
		return parseMessage(messageName, null, null);
	}

	public String parseMessage(String message, String[] variables,
			String[] replacements) {
		String raw = parseRaw(message, variables, replacements);
		String prefix = messages.get("prefix");
		if (prefix != null) {
			return ChatColor.translateAlternateColorCodes('&', prefix + raw);
		} else {
			return "Could not find message " + prefix + ".";
		}
	}

	public String parseRaw(String messageName) {
		return parseRaw(messageName, null, null);
	}

	public String parseRaw(String messageName, String[] variables,
			String[] replacements) {
		messageName = messageName.toLowerCase();
		String prefix = messages.get(messageName.split("\\.")[0] + ".prefix");
		String raw = messages.get(messageName);
		if (raw != null) {
			if (variables != null && replacements != null) {
				if (variables.length != replacements.length) {
					return "The variables and replacements don't match in size! Please alert a developer.";
				}
				for (int i = 0; i < variables.length; i++) {
					// Sanitise replacements
					String replacement = replacements[i].replace("\\", "\\\\")
							.replace("$", "\\$");
					raw = raw.replaceAll("(?iu)\\{" + variables[i] + "\\}",
							replacement);
				}
			}
			raw = raw.replaceAll("(?iu)\\{PREFIX\\}",
					((prefix != null) ? prefix : ""));
			return ChatColor.translateAlternateColorCodes('&',
					((prefix != null) ? prefix : "") + raw);
		} else {
			return "Could not find message " + messageName + ".";
		}
	}

	public void sendMessage(CommandSender cs, String messageName) {
		sendMessage(cs, messageName, null, null);
	}

	public void sendMessage(CommandSender cs, String messageName,
			String[] variables, String[] replacements) {
		if (cs != null) {
			cs.sendMessage(parseMessage(messageName, variables, replacements));
		}
	}

	public void sendRaw(CommandSender cs, String messageName) {
		sendRaw(cs, messageName, null, null);
	}

	public void sendRaw(CommandSender cs, String messageName,
			String[] variables, String[] replacements) {
		if (cs != null) {
			cs.sendMessage(parseRaw(messageName, variables, replacements));
		}
	}
}
