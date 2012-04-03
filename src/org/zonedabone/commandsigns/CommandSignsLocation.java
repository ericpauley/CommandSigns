package org.zonedabone.commandsigns;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CommandSignsLocation {
	
	public static CommandSignsLocation fromFileString(String fs) {
		String[] data = fs.split(",");
		if (data.length == 4 || data.length == 3) {
			int x = Integer.parseInt(data[0]);
			int y = Integer.parseInt(data[1]);
			int z = Integer.parseInt(data[2]);
			if (data.length == 4) {
				CommandSignsLocation csl = new CommandSignsLocation(x, y, z, Bukkit.getWorld(data[3]));
				if (csl.check()) {
					return csl;
				} else {
					return null;
				}
			} else {
				for (World w : Bukkit.getWorlds()) {
					Block b = w.getBlockAt(x, y, z);
					if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
						return new CommandSignsLocation(x, y, z, w);
					}
				}
				return null;
			}
		} else {
			return null;
		}
	}
	private World world;
	private int x;
	private int y;
	private int z;
	
	CommandSignsLocation(int x, int y, int z, World world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
	}
	
	public boolean check() {
		Block b = world.getBlockAt(x, y, z);
		return b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN;
	}
	
	@Override
	public boolean equals(Object object) {
		CommandSignsLocation location = (CommandSignsLocation) object;
		return x == location.getX() && y == location.getY() && z == location.getZ() && world.getName() == location.getWorld().getName();
	}
	
	public World getWorld() {
		return world;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	@Override
	public int hashCode() {
		return x + y + z;
	}
	
	public String toFileString() {
		return x + "," + y + "," + z + "," + world.getName();
	}
}
