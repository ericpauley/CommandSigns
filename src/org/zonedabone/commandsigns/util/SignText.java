package org.zonedabone.commandsigns.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SignText implements Iterable<String> {

	private boolean enabled = true;
	private String owner;
	private boolean redstone = false;
	private List<String> text;
	private final Map<String, Long> timeouts = new HashMap<String, Long>();

	public SignText(String owner, boolean redstone) {
		this.owner = owner;
		text = new ArrayList<String>();
		this.redstone = redstone;
	}

	public SignText clone(String owner) {
		SignText cst = new SignText(owner, redstone);
		for (String s : text) {
			cst.getText().add(s);
		}
		return cst;
	}

	public int count() {
		int size = text.size();
		// Count from last to first, stop whenever a non-blank
		// is found, or if the size hits 'zero'
		while (size > 0 && getLine(size) == "")
			size--;
		return size;
	}

	// Internal list is ZERO indexed, one indexed externally only
	public String getLine(int index) {
		return text.get(index - 1);
	}

	public void addLine(String string) {
		text.add(string);
	}

	public void removeLine(int index) {
		if (index >= 1 && index <= text.size()) {
			text.remove(index - 1);
		}
	}

	public List<String> getText() {
		return this.text;
	}

	public String getOwner() {
		return owner;
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

	public void setLine(int index, String line) {
		while (text.size() < index) {
			text.add("");
		}
		text.set(index - 1, line);
	}

	public void setRedstone(boolean redstone) {
		this.redstone = redstone;
	}

	public void trim() {
		int blank;
		while ((blank = text.lastIndexOf("")) >= 0)
			text.remove(blank);
		for (String line : text) {
			line.trim();
		}
	}

	@Override
	public String toString() {
		String string = "";

		return string;
	}

	@Override
	public Iterator<String> iterator() {
		return text.iterator();
	}
}
