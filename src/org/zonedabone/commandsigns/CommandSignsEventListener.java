package org.zonedabone.commandsigns;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
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

	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getClickedBlock() == null)
			return;
		final CommandSignsClickHandler signClickEvent = new CommandSignsClickHandler(plugin, event.getPlayer(), event.getClickedBlock());
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			signClickEvent.onInteract(event.getAction());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("commandsigns.update")) {
			if (plugin.updateHandler.newAvailable) {
				if (!plugin.getUpdateFile().exists()) {
					Messaging.sendMessage(event.getPlayer(), "update.notify", "v", plugin.updateHandler.newestVersion.toString());
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

	public void handleRedstone(Block b) {
		Location csl = b.getLocation();
		CommandSignsText cst = plugin.activeSigns.get(csl);
		if (cst != null && cst.isRedstone() && !plugin.redstone.contains(csl)) {
			plugin.redstone.add(csl);
			new CommandSignExecutor(plugin, null, csl, null);
		}
	}
}
