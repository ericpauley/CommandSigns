package org.zonedabone.commandsigns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.zonedabone.commandsigns.utils.PlayerState;
import org.zonedabone.commandsigns.utils.SignText;

public class ClickHandler {

	private Location location;
	private Player player;
	private CommandSigns plugin;

	public ClickHandler(CommandSigns plugin, Player player,
			Block block) {
		this.plugin = plugin;
		this.player = player;
		location = block.getLocation();
	}

	public void copySign() {
		SignText text = plugin.activeSigns.get(location);
		if (text == null) {
			plugin.messenger.sendMessage(player, "failure.not_a_sign");
			return;
		}
		SignText clone = plugin.activeSigns.get(location).clone(
				player.getName());
		plugin.playerText.put(player, clone);
		readSign(true);
		plugin.messenger.sendMessage(player, "success.copied");
		plugin.playerStates.put(player, PlayerState.ENABLE);
	}

	public void createSign(boolean batch) {
		if (plugin.activeSigns.containsKey(location)) {
			plugin.messenger.sendMessage(player, "failure.already_enabled");
			return;
		}
		SignText text = plugin.playerText.get(player);

		try {
			text.trim();
			plugin.activeSigns.put(location, text.clone(player.getName()));
			plugin.messenger.sendMessage(player, "success.enabled");
		} catch (Exception e) {
			plugin.messenger.sendMessage(player, "failure.wrong_syntax");
		}

		if (!batch) {
			plugin.playerStates.remove(player);
			plugin.playerText.remove(player);
		}
	}

	public void editSign() {
		SignText cst = plugin.activeSigns.get(location);
		if (cst == null) {
			plugin.messenger.sendMessage(player, "failure.not_a_sign");
			return;
		}
		plugin.messenger.sendMessage(player, "progress.edit_started");
		plugin.playerText.put(player, cst);
		plugin.playerStates.put(player, PlayerState.EDIT);
	}

	public void insert(boolean batch) {
		SignText currentText = plugin.activeSigns.get(location);
		if (currentText == null) {
			plugin.messenger.sendMessage(player, "failure.not_a_sign");
			return;
		}
		SignText newText = plugin.playerText.get(player);

		// Insert lines from last to first - that way you don't overwrite stuff
		for (int i = newText.count(); i >= 1; i--) {
			// Move all lines after the current position up one place
			for (int j = currentText.count(); j >= i; j--) {
				currentText.setLine(j + 1, currentText.getLine(j));
			}
			currentText.setLine(i, newText.getLine(i));
		}
		currentText.trim();

		plugin.messenger.sendMessage(player, "success.done_editing");
		if (!batch) {
			plugin.playerStates.remove(player);
			plugin.playerText.remove(player);
		}
	}

	public boolean onInteract(Action action) {
		PlayerState state = plugin.playerStates.get(player);
		if (state != null) {
			switch (state) {
			case ENABLE:
				createSign(false);
				break;
			case BATCH_ENABLE:
				createSign(true);
				break;
			case INSERT:
				insert(false);
				break;
			case BATCH_INSERT:
				insert(true);
				break;
			case REMOVE:
				removeSign(false);
				break;
			case BATCH_REMOVE:
				removeSign(true);
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
				return new SignExecutor(plugin, player, location, action)
						.runLines();
			}
			return true;
		} else {
			return new SignExecutor(plugin, player, location, action)
					.runLines();
		}
	}

	public void readSign(boolean batch) {
		SignText text = plugin.activeSigns.get(location);
		if (text == null) {
			plugin.messenger.sendMessage(player, "failure.not_a_sign");
			return;
		}
		int i = 1;
		for (String line : text) {
			if (!line.equals("")) {
				plugin.messenger.sendRaw(player, "success.line_print", new String[] {
						"NUMBER", "LINE" }, new String[] { "" + i, line });
			}
			i++;
		}
		if (!batch)
			plugin.playerStates.remove(player);
	}

	public void redstoneToggle(boolean batch) {
		if (!plugin.activeSigns.containsKey(location)) {
			plugin.messenger.sendMessage(player, "failure.not_a_sign");
			return;
		}
		SignText text = plugin.activeSigns.get(location);
		plugin.activeSigns.remove(location);
		boolean enabled = text.isRedstone();
		if (enabled) {
			text.setRedstone(false);
			plugin.activeSigns.put(location, text);
			plugin.messenger.sendMessage(player, "success.redstone_disabled");
		} else {
			text.setRedstone(true);
			plugin.activeSigns.put(location, text);
			plugin.messenger.sendMessage(player, "success.redstone_enabled");
		}
		if (!batch)
			plugin.playerStates.remove(player);
	}

	public void removeSign(boolean batch) {
		if (!plugin.activeSigns.containsKey(location)) {
			plugin.messenger.sendMessage(player, "failure.not_a_sign");
			return;
		}
		plugin.activeSigns.remove(location);
		plugin.messenger.sendMessage(player, "success.removed");
		if (!batch) {
			if (plugin.playerText.containsKey(player)) {
				plugin.playerStates.put(player, PlayerState.ENABLE);
				plugin.messenger.sendMessage(player, "information.text_in_clipboard");
			} else {
				plugin.playerStates.remove(player);
			}
		}
	}

	public void toggleSign(boolean batch) {
		if (!plugin.activeSigns.containsKey(location)) {
			plugin.messenger.sendMessage(player, "failure.not_a_sign");
			return;
		}
		SignText text = plugin.activeSigns.get(location);
		plugin.activeSigns.remove(location);
		boolean enabled = text.isEnabled();
		if (enabled) {
			text.setEnabled(false);
			plugin.activeSigns.put(location, text);
			plugin.messenger.sendMessage(player, "success.disabled");
		} else {
			text.setEnabled(true);
			plugin.activeSigns.put(location, text);
			plugin.messenger.sendMessage(player, "success.enabled");
		}
		if (!batch)
			plugin.playerStates.remove(player);
	}
}
