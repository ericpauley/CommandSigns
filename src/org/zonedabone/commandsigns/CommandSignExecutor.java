package org.zonedabone.commandsigns;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.zonedabone.commandsigns.handlers.ClickTypeHandler;
import org.zonedabone.commandsigns.handlers.CommandHandler;
import org.zonedabone.commandsigns.handlers.CooldownHandler;
import org.zonedabone.commandsigns.handlers.GroupHandler;
import org.zonedabone.commandsigns.handlers.Handler;
import org.zonedabone.commandsigns.handlers.MoneyHandler;
import org.zonedabone.commandsigns.handlers.PermissionHandler;
import org.zonedabone.commandsigns.handlers.RandomHandler;
import org.zonedabone.commandsigns.handlers.SendHandler;
import org.zonedabone.commandsigns.handlers.WaitHandler;

public class CommandSignExecutor {

	private final CommandSigns plugin;
	private final Player player;
	private final Location location;
	private LinkedList<String> lines;
	private final Stack<Boolean> restrictions = new Stack<Boolean>();
	private final Action action;
	private double wait;
	private final CommandSignsText text;

	private static Set<Handler> handlers = new HashSet<Handler>();

	static {
		registerHandler(new CooldownHandler());
		registerHandler(new WaitHandler());
		registerHandler(new PermissionHandler());
		registerHandler(new GroupHandler());
		registerHandler(new MoneyHandler());
		registerHandler(new ClickTypeHandler());
		registerHandler(new SendHandler());
		registerHandler(new CommandHandler());
		registerHandler(new RandomHandler());
	}

	public CommandSignExecutor(CommandSigns plugin, Player player, Location location, Action action) {
		this.plugin = plugin;
		this.player = player;
		this.action = action;
		this.location = location;
		this.text = plugin.activeSigns.get(location);
		if (text != null && text.isEnabled()) {
			if (player == null || plugin.hasPermission(player, "commandsigns.use.regular")) {
				lines = parseCommandSign(player, location);
			} else {
				lines = new LinkedList<String>();
			}
			runLines();
		}
	}

	public static void registerHandler(Handler handler) {
		handlers.add(handler);
	}

	public static void unregisterAll() {
		handlers.clear();
	}

	private void runLines() {
		wait = 0;
		while (wait == 0 && !lines.isEmpty()) {
			String currentLine = lines.poll();
			if (currentLine.equals(""))
				continue;

			boolean silent = false;
			boolean negate = false;
			boolean meta = false;
			do {
				meta = false;
				// The '-' delimiter ends the current restriction block
				if (currentLine.startsWith("-") && !restrictions.isEmpty()) {
					restrictions.pop();
					currentLine = currentLine.substring(1);
					meta = true;
				}
				// If the restriction begins with a ?, make it silent
				else if (currentLine.startsWith("?")) {
					silent = true;
					currentLine = currentLine.substring(1);
					meta = true;
				}
				// If the restriction starts with a !, negate the block
				else if (currentLine.startsWith("!")) {
					negate = true;
					currentLine = currentLine.substring(1);
					meta = true;
				}
			} while (meta == true);

			// If an empty line is negated, invert the top of the stack. (For
			// else)
			if (currentLine.equals("")) {
				if (negate && !restrictions.isEmpty())
					restrictions.push(!restrictions.pop());
				continue;
			}

			// If a restriction block is denied, skip to next line
			if (!restrictions.isEmpty() && restrictions.peek().equals(false))
				continue;

			for (Handler h : handlers) {
				h.handle(this, currentLine, silent, negate);
			}

		}
		if (wait != 0) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				@Override
				public void run() {
					runLines();
				}

			}, (long) (wait * 20));
		}
	}

	private LinkedList<String> parseCommandSign(Player player, Location loc) {
		LinkedList<String> commandList = new LinkedList<String>();
		CommandSignsText commandSign = plugin.activeSigns.get(location);
		for (String line : commandSign.getText()) {
			line = line.replaceAll("(?iu)<blockx>", "" + loc.getX());
			line = line.replaceAll("(?iu)<blocky>", "" + loc.getY());
			line = line.replaceAll("(?iu)<blockz>", "" + loc.getZ());
			line = line.replaceAll("(?iu)<world>", loc.getWorld().getName());
			Player clp = null;
			int dist = Integer.MAX_VALUE;
			for (Player p : loc.getWorld().getPlayers()) {
				if (p.getLocation().distanceSquared(loc) < dist) {
					clp = p;
				}
			}
			if (clp != null) {
				line = line.replaceAll("(?iu)<near>", clp.getName());
			}
			if (player != null) {
				line = line.replaceAll("(?iu)<x>", "" + player.getLocation().getBlockX());
				line = line.replaceAll("(?iu)<y>", "" + player.getLocation().getBlockY());
				line = line.replaceAll("(?iu)<z>", "" + player.getLocation().getBlockZ());
				line = line.replaceAll("(?iu)<name>", "" + player.getName());
				line = line.replaceAll("(?iu)<display>", player.getDisplayName());
				if (CommandSigns.economy != null && CommandSigns.economy.isEnabled()) {
					line = line.replaceAll("(?iu)<money>", "" + CommandSigns.economy.getBalance(player.getName()));
					line = line.replaceAll("(?iu)<formatted>", CommandSigns.economy.format(CommandSigns.economy.getBalance(player.getName())));
				}
			}
			commandList.add(line);
		}
		return commandList;
	}

	public double getWait() {
		return wait;
	}

	public void setWait(double wait) {
		this.wait = wait;
	}

	public static Set<Handler> getHandlers() {
		return handlers;
	}

	public static void setHandlers(Set<Handler> handlers) {
		CommandSignExecutor.handlers = handlers;
	}

	public CommandSigns getPlugin() {
		return plugin;
	}

	public Player getPlayer() {
		return player;
	}

	public Location getLocation() {
		return location;
	}

	public LinkedList<String> getLines() {
		return lines;
	}

	public Stack<Boolean> getRestrictions() {
		return restrictions;
	}

	public Action getAction() {
		return action;
	}

	public CommandSignsText getText() {
		return text;
	}

}
