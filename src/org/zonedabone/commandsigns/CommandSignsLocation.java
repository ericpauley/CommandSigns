package org.zonedabone.commandsigns;

public class CommandSignsLocation {
	private int x;
	private int y;
	private int z;
	
	CommandSignsLocation(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getZ() {
		return this.z;
	}
	
	@Override
	public boolean equals(Object object) {
		CommandSignsLocation location = (CommandSignsLocation)object;
		if(this.x == location.getX() && this.y == location.getY() && this.z == location.getZ()) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return(this.x + this.y + this.z);
	}
	
	public String toFileString() {
		String string;
		string = (this.x + "," + this.y + "," + this.z);
		return string;
	}
}
