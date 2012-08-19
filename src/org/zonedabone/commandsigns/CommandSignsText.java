package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;

public class CommandSignsText {
	
	private String owner;
	private List<String> text;
	private boolean redstone = false;
	
	public boolean isRedstone() {
		return redstone;
	}
	
	public void setRedstone(boolean redstone) {
		this.redstone = redstone;
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
	
	public void trim() {
		int blank;
		while ((blank = text.indexOf("")) > -1)
			text.remove(blank);
		for (String line : text)
			line.trim();
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
}
