package org.zonedabone.commandsigns;

public class CommandSignsText {

	private String[] text = new String[10];

	CommandSignsText() {
		for (int i = 0; i < 10; i++) {
			this.text[i] = null;
		}
	}

	public String[] getText() {
		return this.text;
	}

	@Override
	public String toString() {
		String string = "";
		String line;
		for (int i = 0; i < 10; i++) {
			line = this.getLine(i);
			if (line != null) {
				string = string.concat(this.getLine(i) + ((i != 9) ? " " : ""));
			}
		}
		return string;
	}

	public String getLine(int index) {
		if (index < 0 || index >= 10) {
			return null;
		}
		return this.text[index];
	}

	public boolean setLine(int index, String line) {
		if (index < 0 || index >= 10) {
			return false;
		}
		this.text[index] = line;
		return true;
	}

	public String toFileString() {
		String string = "";
		String line;
		for (int i = 0; i < 10; i++) {
			line = this.getLine(i);
			if (line != null && !line.equals("")) {
				string = string.concat(this.getLine(i) + "[LINEBREAK]");
			}
		}
		return string;
	}
}
