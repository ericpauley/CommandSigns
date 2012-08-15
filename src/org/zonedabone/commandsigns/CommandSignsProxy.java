package org.zonedabone.commandsigns;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class CommandSignsProxy implements CommandSender {
	
	private CommandSender original;
	private CommandSender receiver;
	private boolean silent;
	
	public CommandSignsProxy(CommandSender original, CommandSender receiver, boolean silent) {
		this.original = original;
		this.receiver = receiver;
		this.silent = silent;
	}
	
	@Override
	public boolean isOp() {
		return original.isOp();
	}

	@Override
	public boolean isPermissionSet(String name) {
		return original.isPermissionSet(name);
	}
	
	@Override
	public void setOp(boolean value) {
		original.setOp(value);
	}
	
	@Override
	public Server getServer() {
		return original.getServer();
	}
	
	@Override
	public boolean isPermissionSet(Permission perm) {
		return original.isPermissionSet(perm);
	}
	
	@Override
	public String getName() {
		return original.getName();
	}
	
	@Override
	public boolean hasPermission(String name) {
		return original.hasPermission(name);
	}
	
	@Override
	public boolean hasPermission(Permission perm) {
		return original.hasPermission(perm);
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		return original.addAttachment(plugin, name, value);
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin plugin) {
		return original.addAttachment(plugin);
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
		return original.addAttachment(plugin, name, value, ticks);
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		return original.addAttachment(plugin, ticks);
	}
	
	@Override
	public void removeAttachment(PermissionAttachment attachment) {
		original.removeAttachment(attachment);
	}
	
	@Override
	public void recalculatePermissions() {
		original.recalculatePermissions();
	}
	
	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return original.getEffectivePermissions();
	}
	
	@Override
	public void sendMessage(String message) {
		if (receiver != null && !silent) {
			receiver.sendMessage(message);
		}
	}
	
	@Override
	public void sendMessage(String[] messages) {
		if (receiver != null && !silent) {
			receiver.sendMessage(messages);
		}
	}
}
