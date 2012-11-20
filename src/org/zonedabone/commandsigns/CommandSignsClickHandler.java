package org.zonedabone.commandsigns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class CommandSignsClickHandler {

	private Location location;
	private Player player;
	private CommandSigns plugin;

	public CommandSignsClickHandler(CommandSigns plugin, Player player,
			Block block) {
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
		CommandSignsText clone = plugin.activeSigns.get(location).clone(
				player.getName());
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
		Messaging.sendMessage(player, "success.removed");
		if (!batch) {
			if (plugin.playerText.containsKey(player)) {
				plugin.playerStates.put(player, CommandSignsPlayerState.ENABLE);
				Messaging.sendMessage(player, "information.text_in_clipboard");
			} else {
				plugin.playerStates.remove(player);
			}
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

	public void enableSign(boolean batch) {
		if (plugin.activeSigns.containsKey(location)) {
			Messaging.sendMessage(player, "failure.already_enabled");
			return;
		}
		CommandSignsText text = plugin.playerText.get(player);
		text.trim();
		plugin.activeSigns.put(location, text.clone(player.getName()));

		Messaging.sendMessage(player, "success.enabled");
		if (!batch) {
			plugin.playerStates.remove(player);
			plugin.playerText.remove(player);
		}
	}

	public void insert(boolean batch) {
		CommandSignsText currentText = plugin.activeSigns.get(location);
		if (currentText == null) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		CommandSignsText newText = plugin.playerText.get(player);

		// Insert lines from last to first - that way you don't overwrite stuff
		for (int i = newText.count(); i >= 1; i--) {
			// Move all lines after the current position up one place
			for (int j = currentText.count(); j >= i; j--) {
				currentText.setLine(j + 1, currentText.getLine(j));
			}
			currentText.setLine(i, newText.getLine(i));
		}
		currentText.trim();

		Messaging.sendMessage(player, "success.done_editing");
		if (!batch) {
			plugin.playerStates.remove(player);
			plugin.playerText.remove(player);
		}
	}

	public boolean onInteract(Action action) {
		CommandSignsPlayerState state = plugin.playerStates.get(player);
		if (state != null) {
			switch (state) {
			case ENABLE:
				enableSign(false);
				break;
			case BATCH_ENABLE:
				enableSign(true);
				break;
			case INSERT:
				insert(false);
				break;
			case BATCH_INSERT:
				insert(true);
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
				readSign(true);
				break;
			case TOGGLE:
				toggleSign(false);
				break;
			case BATCH_TOGGLE:
				toggleSign(true);
				break;
			case REDSTONE:
				redstoneToggle(false);
				break;
			case BATCH_REDSTONE:
				redstoneToggle(true);
				break;
			default:
				return new CommandSignExecutor(plugin, player, location, action)
						.runLines();
			}
			return true;
		} else {
			return new CommandSignExecutor(plugin, player, location, action)
					.runLines();
		}
	}

	public void readSign(boolean batch) {
		CommandSignsText text = plugin.activeSigns.get(location);
		if (text == null) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		int i = 1;
		for (String line : text) {
			if (!line.equals("")) {
				Messaging.sendRaw(player, "success.line_print", new String[] {
						"NUMBER", "LINE" }, new String[] { "" + i, line });
			}
			i++;
		}
		if (!batch)
			plugin.playerStates.remove(player);
	}

	public void redstoneToggle(boolean batch) {
		if (!plugin.activeSigns.containsKey(location)) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		CommandSignsText text = plugin.activeSigns.get(location);
		plugin.activeSigns.remove(location);
		boolean enabled = text.isRedstone();
		if (enabled) {
			text.setRedstone(false);
			plugin.activeSigns.put(location, text);
			Messaging.sendMessage(player, "success.redstone_disabled");
		} else {
			text.setRedstone(true);
			plugin.activeSigns.put(location, text);
			Messaging.sendMessage(player, "success.redstone_enabled");
		}
		if (!batch)
			plugin.playerStates.remove(player);
	}

	public void toggleSign(boolean batch) {
		if (!plugin.activeSigns.containsKey(location)) {
			Messaging.sendMessage(player, "failure.not_a_sign");
			return;
		}
		CommandSignsText text = plugin.activeSigns.get(location);
		plugin.activeSigns.remove(location);
		boolean enabled = text.isEnabled();
		if (enabled) {
			text.setEnabled(false);
			plugin.activeSigns.put(location, text);
			Messaging.sendMessage(player, "success.disabled");
		} else {
			text.setEnabled(true);
			plugin.activeSigns.put(location, text);
			Messaging.sendMessage(player, "success.enabled");
		}
		if (!batch)
			plugin.playerStates.remove(player);
	}
}
