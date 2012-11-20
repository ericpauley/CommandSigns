package org.zonedabone.commandsigns;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.zonedabone.commandsigns.CommandSignsUpdater.Version;

public class Messaging {

	private static Map<String, String> messages = new ConcurrentHashMap<String, String>();

	public static void loadMessages(CommandSigns plugin) {
		File f = new File(plugin.getDataFolder(), "messages.yml");
		Configuration included = YamlConfiguration.loadConfiguration(plugin
				.getResource("messages.yml"));
		if (!f.exists()) {
			plugin.getLogger().info("Creating default messages.yml.");
			plugin.saveResource("messages.yml", true);
		}

		Configuration config = YamlConfiguration.loadConfiguration(f);
		CommandSignsUpdater updaterClass = new CommandSignsUpdater(plugin);

		Version curVersion = updaterClass.new Version(
				config.getString("version"));
		Version incVersion = updaterClass.new Version(
				included.getString("version"));

		if (incVersion.compareTo(curVersion) > 0) {
			plugin.getLogger().info("Updating messages.yml.");
			plugin.saveResource("messages.yml", true);
		}

		config = YamlConfiguration.loadConfiguration(f);
		for (String k : config.getKeys(true)) {
			if (config.isString(k)) {
				messages.put(k, config.getString(k));
			}
		}
		plugin.getLogger().info("Loaded " + messages.size() + " messages.");
	}

	public static String parseMessage(String messageName) {
		return parseMessage(messageName, null, null);
	}

	public static String parseMessage(String message, String[] variables,
			String[] replacements) {
		String raw = parseRaw(message, variables, replacements);
		String prefix = messages.get("prefix");
		if (prefix != null) {
			return ChatColor.translateAlternateColorCodes('&', prefix + raw);
		} else {
			return "Could not find message " + prefix + ".";
		}
	}

	public static String parseRaw(String messageName) {
		return parseRaw(messageName, null, null);
	}

	public static String parseRaw(String messageName, String[] variables,
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

	public static void sendMessage(CommandSender cs, String messageName) {
		sendMessage(cs, messageName, null, null);
	}

	public static void sendMessage(CommandSender cs, String messageName,
			String[] variables, String[] replacements) {
		if (cs != null) {
			cs.sendMessage(parseMessage(messageName, variables, replacements));
		}
	}

	public static void sendRaw(CommandSender cs, String messageName) {
		sendRaw(cs, messageName, null, null);
	}

	public static void sendRaw(CommandSender cs, String messageName,
			String[] variables, String[] replacements) {
		if (cs != null) {
			cs.sendMessage(parseRaw(messageName, variables, replacements));
		}
	}
}
