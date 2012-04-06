package org.zonedabone.commandsigns;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
		Block block = event.getBlock();
		if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
			CommandSignsLocation location = new CommandSignsLocation(block.getWorld(), block.getX(), block.getY(), block.getZ());
			if (plugin.activeSigns.containsKey(location)) {
				event.getPlayer().sendMessage("§cCommandSign text must be removed first.");
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final CommandSignsSignClickEvent signClickEvent = new CommandSignsSignClickEvent(plugin);
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			BlockState state = event.getClickedBlock().getState();
			if (state instanceof Sign) {
				final Sign sign = (Sign) state;
				new Thread(){
					@Override
					public void run() {
						signClickEvent.onRightClick(event, sign);
					}
				}.start();
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if(e.getPlayer().hasPermission("commandsigns.update")){
			if(plugin.version < plugin.newestVersion){
				if(!plugin.getUpdateFile().exists()){
					e.getPlayer().sendMessage(ChatColor.YELLOW+"CommandSigns is updated to "+ChatColor.DARK_PURPLE+plugin.stringNew+ChatColor.YELLOW+". You can update your install with "+ChatColor.DARK_PURPLE+"/cs update"+ChatColor.YELLOW+".");
				}
			}
		}
	}
}
