package org.zonedabone.commandsigns.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.zonedabone.commandsigns.CommandSigns;

public abstract class ConfigStore implements Map<String, String> {

	private Map<String, String> config = new ConcurrentHashMap<String, String>();
	protected CommandSigns plugin;
	
	public ConfigStore(CommandSigns plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Loads the configuration file into memory
	 */
	public abstract void load();
	
	@Override
	public void clear() {
		config.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return config.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return config.containsValue(value);
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return config.entrySet();
	}

	@Override
	public String get(Object key) {
		return config.get(key);
	}
	
	/**
	 * Gets the boolean value mapped to the given key.
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(Object key) {
		try {
			return Boolean.parseBoolean(this.get(key));
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * Gets the integer value mapped to the given key.
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(Object key) {
		try {
			return Integer.parseInt(this.get(key));
		} catch (Exception ex) {
			return 0;
		}
	}

	@Override
	public boolean isEmpty() {
		return config.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return config.keySet();
	}

	@Override
	public String put(String key, String value) {
		return config.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		config.putAll(m);
	}

	@Override
	public String remove(Object key) {
		return config.remove(key);
	}

	@Override
	public int size() {
		return config.size();
	}

	@Override
	public Collection<String> values() {
		return config.values();
	}
	
}
