package org.zonedabone.commandsigns;

import org.bukkit.Location;
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
		final CommandSignsClickHandler signClickEvent = new CommandSignsClickHandler(plugin, event.getPlayer(), event.getClickedBlock());
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
			new Thread() {
				
				@Override
				public void run() {
					signClickEvent.onRightClick();
				}
			}.start();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("commandsigns.update")) {
			if (plugin.version < plugin.newestVersion) {
				if (!plugin.getUpdateFile().exists()) {
					Messaging.sendMessage(event.getPlayer(), "update.notify", "v", plugin.stringNew);
				}
			}
		}
	}
	
	@EventHandler
	public void onRedstoneChange(BlockRedstoneEvent event) {
		if (event.getNewCurrent() != 0 && event.getOldCurrent() == 0) {
			Location csl = event.getBlock().getLocation();
			CommandSignsText cst = plugin.activeSigns.get(csl);
			if (cst != null && cst.isRedstone()) {
				final CommandSignsClickHandler signClickEvent = new CommandSignsClickHandler(plugin, null, event.getBlock());
				new Thread() {
					
					@Override
					public void run() {
						signClickEvent.runSign();
					}
				}.start();
			}
		}
	}
}
