package org.zonedabone.commandsigns.listener;

import net.gravitydevelopment.updater.Updater;

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
import org.zonedabone.commandsigns.ClickHandler;
import org.zonedabone.commandsigns.CommandSigns;
import org.zonedabone.commandsigns.SignExecutor;
import org.zonedabone.commandsigns.util.SignText;

public class EventListener implements Listener {

	private CommandSigns plugin;

	public EventListener(CommandSigns plugin) {
		this.plugin = plugin;
	}

	public void handleRedstone(Block b) {
		Location csl = b.getLocation();
		SignText text = plugin.activeSigns.get(csl);
		if (text != null && text.isRedstone()) {
			new SignExecutor(plugin, null, csl, null).runLines();
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Location location = event.getBlock().getLocation();
		if (plugin.activeSigns.containsKey(location)) {
			plugin.messenger.sendMessage(event.getPlayer(),
					"failure.remove_first");
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
				final ClickHandler signClickEvent = new ClickHandler(plugin,
						event.getPlayer(), block);
				if (signClickEvent.onInteract(action)
						&& action != Action.PHYSICAL) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.hasPermission(event.getPlayer(), "commandit.update", false)) {
            if (plugin.updater != null && plugin.updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                plugin.messenger.sendMessage(event.getPlayer(), "update.notify", new String[] { "VERSION" }, new String[] { plugin.updater.getLatestName() });
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
