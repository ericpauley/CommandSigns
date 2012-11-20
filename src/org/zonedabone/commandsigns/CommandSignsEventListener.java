package org.zonedabone.commandsigns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class CommandSignsEventListener implements Listener {

	private CommandSigns plugin;

	public CommandSignsEventListener(CommandSigns plugin) {
		this.plugin = plugin;
	}

	public void handleRedstone(Block b) {
		Location csl = b.getLocation();
		CommandSignsText text = plugin.activeSigns.get(csl);
		if (text != null && text.isRedstone()) {
			new CommandSignExecutor(plugin, null, csl, null).runLines();
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Location location = event.getBlock().getLocation();
		if (plugin.activeSigns.containsKey(location)) {
			Messaging.sendMessage(event.getPlayer(), "failure.remove_first");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		Block block = null;
		Action action = event.getAction();
		if (action == Action.RIGHT_CLICK_BLOCK
				|| action == Action.LEFT_CLICK_BLOCK
				|| action == Action.PHYSICAL) {
			block = event.getClickedBlock();
			if (block != null) {
				final CommandSignsClickHandler signClickEvent = new CommandSignsClickHandler(
						plugin, event.getPlayer(), block);
				if (signClickEvent.onInteract(action)
						&& action != Action.PHYSICAL) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("commandsigns.update")) {
			if (plugin.updateHandler.newAvailable) {
				if (!plugin.getUpdateFile().exists()) {
					Messaging.sendMessage(event.getPlayer(), "update.notify",
							new String[] {"VERSION"},
							new String[] {plugin.updateHandler.newestVersion.toString()});
				}
			}
		}
	}

	@EventHandler
	public void onRedstoneChange(BlockRedstoneEvent event) {
		if (event.getNewCurrent() != 0 && event.getOldCurrent() == 0) {
			Block b = event.getBlock();
			handleRedstone(b);
			handleRedstone(b.getRelative(BlockFace.NORTH));
			handleRedstone(b.getRelative(BlockFace.SOUTH));
			handleRedstone(b.getRelative(BlockFace.EAST));
			handleRedstone(b.getRelative(BlockFace.WEST));
			handleRedstone(b.getRelative(BlockFace.UP));
			handleRedstone(b.getRelative(BlockFace.DOWN));
		}
	}
}
