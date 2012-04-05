package org.zonedabone.commandsigns;

import java.util.ArrayList;
import java.util.List;

public class CommandSignsText {
	
	private String owner;
	private List<String> text;
	
	public CommandSignsText(String owner) {
		this.owner = owner;
		this.text = new ArrayList<String>();
	}
	
	public CommandSignsText clone(String owner) {
		CommandSignsText cst = new CommandSignsText(owner);
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
		while (text.get(text.size() - 1).equals("")) {
			text.remove(text.size() - 1);
		}
		for(int i = 0;i<text.size();i++){
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
}
