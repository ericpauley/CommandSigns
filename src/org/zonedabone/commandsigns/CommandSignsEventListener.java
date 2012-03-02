package org.zonedabone.commandsigns;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
		Block block = event.getBlock();
		if (block.getType() == Material.SIGN_POST
				|| block.getType() == Material.WALL_SIGN) {
			CommandSignsLocation location = new CommandSignsLocation(
					block.getX(), block.getY(), block.getZ());
			if (plugin.activeSigns.containsKey(location)) {
				event.getPlayer().sendMessage(
						"§cCommandSign text must be removed first.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		CommandSignsSignClickEvent signClickEvent = new CommandSignsSignClickEvent(
				plugin);
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			BlockState state = event.getClickedBlock().getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				signClickEvent.onRightClick(event, sign);
			}
		}
	}
}
