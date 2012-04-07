package org.zonedabone.commandsigns;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageManager {
	
	private static Map<String, String> messages = new ConcurrentHashMap<String, String>();
	
	public static void loadMessages(JavaPlugin plugin) {
		File f = new File(plugin.getDataFolder(), "messages.yml");
		if (!f.exists()) {
			plugin.getLogger().info("Creating default messages.yml.");
			plugin.saveResource("messages.yml", true);
		}
		Configuration config = YamlConfiguration.loadConfiguration(f);
		for (String k : config.getKeys(true)) {
			if (config.isString(k)) {
				messages.put(k, config.getString(k));
			}
		}
		plugin.getLogger().info("Loaded " + messages.size() + " messages.");
	}
	
	public static String parseMessage(String message, String... replacements) {
		String raw = parseRaw(message, replacements);
		String prefix = messages.get("prefix");
		if (prefix != null) {
			return ChatColor.translateAlternateColorCodes('&', prefix + raw);
		} else {
			return "Could not find message " + prefix + ".";
		}
	}
	
	public static String parseRaw(String message, String... replacements) {
		message = message.toLowerCase();
		String prefix = messages.get(message.split("\\.")[0] + ".prefix");
		String raw = messages.get(message);
		if (raw != null) {
			for (int i = 0; i < replacements.length - 1; i++) {
				raw = raw.replaceAll("(?iu)\\{" + replacements[i] + "[a-zA-Z0-9]*\\}", replacements[i + 1]);
			}
			raw = raw.replaceAll("(?iu)\\{PREFIX\\}", ((prefix != null) ? prefix : ""));
			return ChatColor.translateAlternateColorCodes('&', ((prefix != null) ? prefix : "") + raw);
		} else {
			return "Could not find message " + message + ".";
		}
	}
	
	public static void sendMessage(CommandSender cs, String message, String... replacements) {
		cs.sendMessage(parseMessage(message, replacements));
	}
}
