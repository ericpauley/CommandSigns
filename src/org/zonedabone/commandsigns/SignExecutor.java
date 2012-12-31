package org.zonedabone.commandsigns;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.zonedabone.commandsigns.handler.ChatHandler;
import org.zonedabone.commandsigns.handler.ClickTypeHandler;
import org.zonedabone.commandsigns.handler.CommandHandler;
import org.zonedabone.commandsigns.handler.CooldownHandler;
import org.zonedabone.commandsigns.handler.GroupHandler;
import org.zonedabone.commandsigns.handler.Handler;
import org.zonedabone.commandsigns.handler.MoneyHandler;
import org.zonedabone.commandsigns.handler.PermissionHandler;
import org.zonedabone.commandsigns.handler.RandomHandler;
import org.zonedabone.commandsigns.handler.SendHandler;
import org.zonedabone.commandsigns.handler.WaitHandler;
import org.zonedabone.commandsigns.util.SignText;

public class SignExecutor {

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
		registerHandler(new ChatHandler());
	}

	public static Set<Handler> getHandlers() {
		return handlers;
	}

	public static void registerHandler(Handler handler) {
		handlers.add(handler);
	}

	public static void setHandlers(Set<Handler> handlers) {
		SignExecutor.handlers = handlers;
	}

	public static void unregisterAll() {
		handlers.clear();
	}

	private final Action action;
	boolean isValid = false;
	private LinkedList<String> lines;

	private final Location location;

	private final Player player;

	private final CommandSigns plugin;

	private final Stack<Boolean> restrictions = new Stack<Boolean>();

	private final SignText text;

	private double wait;

	public SignExecutor(CommandSigns plugin, Player player, Location location,
			Action action) {
		this.plugin = plugin;
		this.player = player;
		this.action = action;
		this.location = location;
		this.text = plugin.activeSigns.get(location);
		if (text != null && text.isEnabled()) {
			if (player == null
					|| plugin.hasPermission(player, "commandsigns.use.regular")) {
				lines = parseCommandSign(player, location);
			} else {
				lines = new LinkedList<String>();
			}
			isValid = true;
		}
	}

	public Action getAction() {
		return action;
	}

	public LinkedList<String> getLines() {
		return lines;
	}

	public Location getLocation() {
		return location;
	}

	public Player getPlayer() {
		return player;
	}

	public CommandSigns getPlugin() {
		return plugin;
	}

	public Stack<Boolean> getRestrictions() {
		return restrictions;
	}

	public SignText getText() {
		return text;
	}

	public double getWait() {
		return wait;
	}

	private LinkedList<String> parseCommandSign(Player player, Location loc) {
		LinkedList<String> commandList = new LinkedList<String>();
		SignText commandSign = plugin.activeSigns.get(location);
		for (String line : commandSign) {
			line = line.replaceAll("(?iu)<blockx>", "" + loc.getX());
			line = line.replaceAll("(?iu)<blocky>", "" + loc.getY());
			line = line.replaceAll("(?iu)<blockz>", "" + loc.getZ());
			line = line.replaceAll("(?iu)<world>", loc.getWorld().getName());
			if (line.toLowerCase().contains(("<near>"))) {
				Player clp = null;
				double dist = Double.MAX_VALUE;
				for (Player p : loc.getWorld().getPlayers()) {
					if (p.getWorld().equals(loc.getWorld())) {
						if (p.getLocation().distanceSquared(loc) < dist) {
							clp = p;
						}
					}
				}
				if (clp != null) {
					line = line.replaceAll("(?iu)<near>", clp.getName());
				}
			}
			while (line.toLowerCase().contains("<randomname>")) {
				Player[] randoms = plugin.getServer().getOnlinePlayers();
				int rand = (int) Math.round(Math.random()
						* (randoms.length - 1));
				line = line.replaceFirst("(?iu)<randomname>",
						randoms[rand].getName());
			}
			if (player != null) {
				line = line.replaceAll("(?iu)<x>", ""
						+ player.getLocation().getBlockX());
				line = line.replaceAll("(?iu)<y>", ""
						+ player.getLocation().getBlockY());
				line = line.replaceAll("(?iu)<z>", ""
						+ player.getLocation().getBlockZ());
				line = line.replaceAll("(?iu)<name>", "" + player.getName());
				line = line.replaceAll("(?iu)<player>", "" + player.getName());
				String[] addr = player.getAddress().toString().split("/");
				line = line.replaceAll("(?iu)<ip>",
						"" + addr[addr.length - 1].split(":")[0]);
				line = line.replaceAll("(?iu)<display>",
						player.getDisplayName());
				if (CommandSigns.economy != null
						&& CommandSigns.economy.isEnabled()) {
					line = line
							.replaceAll(
									"(?iu)<money>",
									""
											+ CommandSigns.economy
													.getBalance(player
															.getName()));
					line = line.replaceAll("(?iu)<formatted>",
							CommandSigns.economy.format(CommandSigns.economy
									.getBalance(player.getName())));
				}
			}
			commandList.add(line);
		}
		return commandList;
	}

	public boolean runLines() {
		if (!isValid) {
			return false;
		}
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
			plugin.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {

						@Override
						public void run() {
							runLines();
						}

					}, (long) (wait * 20));
		}
		return true;
	}

	public void setWait(double wait) {
		this.wait = wait;
	}

}
