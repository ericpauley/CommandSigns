package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.OfflinePlayer;

public class CommandSignsText {
	
	private String owner;
	private List<String> text;
	private boolean redstone = false;
	private Map<OfflinePlayer, Long> timeouts = new ConcurrentHashMap<OfflinePlayer, Long>();
	private Map<OfflinePlayer, Long> uses = new ConcurrentHashMap<OfflinePlayer, Long>();
	private long lastUse = 0, numUses = 0;
	private boolean used = false;
	
	public boolean isUsed() {
		return used;
	}
	
	public void setUsed(boolean used) {
		this.used = used;
	}
	
	public boolean isRedstone() {
		return redstone;
	}
	
	public void setRedstone(boolean redstone) {
		this.redstone = redstone;
	}
	
	public long getLastUse(OfflinePlayer p) {
		if (timeouts.get(p) == null) {
			return 0;
		} else {
			return timeouts.get(p);
		}
	}
	
	public void setLastUse(OfflinePlayer p) {
		timeouts.put(p, System.currentTimeMillis());
	}
	
	public long getUses(OfflinePlayer p) {
		if (uses.get(p) == null) {
			return 0;
		} else {
			return uses.get(p);
		}
	}
	
	public long getNumUses() {
		return numUses;
	}
	
	public void use() {
		numUses++;
	}
	
	public void setNumUses(long uses) {
		numUses = uses;
	}
	
	public void use(OfflinePlayer p) {
		timeouts.put(p, getUses(p) + 1);
	}
	
	public CommandSignsText(String owner, boolean redstone) {
		this.owner = owner;
		text = new ArrayList<String>();
		this.redstone = redstone;
	}
	
	public CommandSignsText clone(String owner) {
		CommandSignsText cst = new CommandSignsText(owner, redstone);
		for (String s : text) {
			cst.getText().add(s);
		}
		return cst;
	}
	
	public String getLine(int index) {
		if (index < 0 || index >= text.size()) {
			return null;
		}
		return text.get(index);
	}
	
	public String getOwner() {
		return owner;
	}
	
	public List<String> getText() {
		return text;
	}
	
	public boolean setLine(int index, String line) {
		while (text.size() <= index) {
			text.add("");
		}
		text.set(index, line);
		return true;
	}
	
	public String toFileString() {
		String string = "";
		for (String line : text) {
			if (!string.equals("")) {
				string = string + "\u00A7";
			}
			string = string + line;
		}
		return string;
	}
	
	public void trim() {
		while (text.size() > 0 && text.get(text.size() - 1).equals("")) {
			text.remove(text.size() - 1);
		}
		for (int i = 0; i < text.size(); i++) {
			text.set(i, text.get(i).trim());
		}
	}
	
	@Override
	public String toString() {
		String string = "";
		String line;
		for (int i = 0; i < 10; i++) {
			line = getLine(i);
			if (line != null) {
				string = string.concat(getLine(i) + (i != 9 ? " " : ""));
			}
		}
		return string;
	}
	
	public long getLastUse() {
		return lastUse;
	}
	
	public void setLastUse(long lastUse) {
		this.lastUse = lastUse;
	}
	
	public Map<OfflinePlayer, Long> getTimeouts() {
		return timeouts;
	}
	
	public Map<OfflinePlayer, Long> getUses() {
		return uses;
	}
}
