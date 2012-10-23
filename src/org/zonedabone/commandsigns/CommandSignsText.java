package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandSignsText {

	private boolean enabled = true;
	private String owner;
	private boolean redstone = false;
	private List<String> text;
	private final Map<String, Long> timeouts = new HashMap<String, Long>();

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

	public Map<String, Long> getTimeouts() {
		return timeouts;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isRedstone() {
		return redstone;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean setLine(int index, String line) {
		while (text.size() <= index) {
			text.add("");
		}
		text.set(index, line);
		return true;
	}

	public void setRedstone(boolean redstone) {
		this.redstone = redstone;
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

	public void trim() {
		int blank;
		while ((blank = text.lastIndexOf("")) > 0)
			text.remove(blank);
		for (int i = 0; i < text.size(); i++) {
			text.set(i, text.get(i).trim());
		}
		if (text.size() == 0 || !text.get(0).equals("")) {
			text.add(0, "");
		}
	}
}
