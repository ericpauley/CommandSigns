package org.zonedabone.commandsigns;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class CommandSignsClickHandler {

	private CommandSigns plugin;
	private Player player;
	private Location location;

	public CommandSignsClickHandler(CommandSigns plugin, Player player, Block block) {
		this.plugin = plugin;
		this.player = player;
		location = block.getLocation();
	}

	public void copySign() {
		CommandSignsText text = plugin.activeSigns.get(location);
		if (text == null) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		CommandSignsText clone = plugin.activeSigns.get(location).clone(player.getName());
		plugin.playerText.put(player, clone);
		readSign(true);
		Messaging.sendMessage(player, "success.copied");
		plugin.playerStates.put(player, CommandSignsPlayerState.ENABLE);
	}

	public void disableSign(boolean batch) {
		if (!plugin.activeSigns.containsKey(location)) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		plugin.activeSigns.remove(location);
		if (!batch) {
			if (plugin.playerText.containsKey(player)) {
				plugin.playerStates.put(player, CommandSignsPlayerState.ENABLE);
				player.sendMessage("Sign disabled. You still have text in your clipboard.");
			} else {
				plugin.playerStates.remove(player);
				player.sendMessage("Sign disabled.");
			}
		}else{
			player.sendMessage("Sign disabled.");
		}
	}

	public void enableSign(boolean batch) {
		if (plugin.activeSigns.containsKey(location)) {
			player.sendMessage("Sign is already enabled!");
			return;
		}
		CommandSignsText text = plugin.playerText.get(player);
		plugin.activeSigns.put(location, text.clone(player.getName()));
		if (!batch) {
			plugin.playerStates.remove(player);
			plugin.playerText.remove(player);
		}
		player.sendMessage("CommandSign enabled");
	}

	public void onInteract(Action action) {
		CommandSignsPlayerState state = plugin.playerStates.get(player);
		if (state != null) {
			switch (state) {
			case ENABLE:
				enableSign(false);
				break;
			case BATCH_ENABLE:
				enableSign(true);
				break;
			case REMOVE:
				disableSign(false);
				break;
			case BATCH_REMOVE:
				disableSign(true);
				break;
			case READ:
				readSign(false);
				break;
			case BATCH_READ:
				readSign(true);
				break;
			case COPY:
				copySign();
				break;
			case EDIT_SELECT:
				editSign();
				readSign(false);
				break;
			default:
				Material m = location.getBlock().getType();
				if ((m == Material.WOOD_PLATE || m == Material.STONE_PLATE) && (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK))
					return;
				new CommandSignExecutor(plugin, player, location, action);
				break;
			}
		} else {
			Material m = location.getBlock().getType();
			if ((m == Material.WOOD_PLATE || m == Material.STONE_PLATE) && (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK))
				return;
			new CommandSignExecutor(plugin, player, location, action);
		}
	}

	public void editSign() {
		CommandSignsText cst = plugin.activeSigns.get(location);
		if (cst == null) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		Messaging.sendMessage(player, "progress.edit_started");
		plugin.playerText.put(player, cst);
		plugin.playerStates.put(player, CommandSignsPlayerState.EDIT);
	}

	public void readSign(boolean batch) {
		CommandSignsText text = plugin.activeSigns.get(location);
		if (text == null) {
			player.sendMessage("Sign is not a CommandSign.");
			return;
		}
		int i = 0;
		for (String s : text.getText()) {
			if (!s.equals("")) {
				player.sendMessage(i + ": " + s);
			}
			i++;
		}
		if (!batch)
			plugin.playerStates.remove(player);
	}
}
