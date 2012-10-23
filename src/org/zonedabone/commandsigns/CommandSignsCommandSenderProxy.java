package org.zonedabone.commandsigns;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/**
 * Creates a tapped link between an originator CommandSender and a recipient
 * CommandSender originator and recipient can be the same CommandSender Allows
 * sendMessage() methods to be intercepted if the silent flag is set
 */
public class CommandSignsCommandSenderProxy implements CommandSender {

	private CommandSender originator;
	private CommandSender recipient;
	boolean silent;

	public CommandSignsCommandSenderProxy(CommandSender originator) {
		this(originator, originator, false);
	}

	public CommandSignsCommandSenderProxy(CommandSender originator,
			boolean silent) {
		this(originator, originator, silent);
	}

	public CommandSignsCommandSenderProxy(CommandSender originator,
			CommandSender recipient) {
		this(originator, recipient, false);
	}

	public CommandSignsCommandSenderProxy(CommandSender originator,
			CommandSender recipient, boolean silent) {
		this.originator = originator;
		this.recipient = recipient;
		this.silent = silent;
	}

	public PermissionAttachment addAttachment(Plugin plugin) {
		return originator.addAttachment(plugin);
	}

	public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		return originator.addAttachment(plugin, ticks);
	}

	public PermissionAttachment addAttachment(Plugin plugin, String name,
			boolean value) {
		return originator.addAttachment(plugin, name, value);
	}

	public PermissionAttachment addAttachment(Plugin plugin, String name,
			boolean value, int ticks) {
		return originator.addAttachment(plugin, name, value, ticks);
	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return originator.getEffectivePermissions();
	}

	public String getName() {
		return originator.getName();
	}

	public Server getServer() {
		return originator.getServer();
	}

	public boolean hasPermission(Permission perm) {
		return originator.hasPermission(perm);
	}

	public boolean hasPermission(String name) {
		return originator.hasPermission(name);
	}

	public boolean isOp() {
		return originator.isOp();
	}

	public boolean isPermissionSet(Permission perm) {
		return originator.isPermissionSet(perm);
	}

	public boolean isPermissionSet(String name) {
		return originator.isPermissionSet(name);
	}

	public boolean isSilent() {
		return silent;
	}

	public void recalculatePermissions() {
		originator.recalculatePermissions();
	}

	public void removeAttachment(PermissionAttachment attachment) {
		originator.removeAttachment(attachment);
	}

	public void sendMessage(String message) {
		if (!silent && recipient != null) {
			recipient.sendMessage(message);
		}
	}

	public void sendMessage(String[] messages) {
		if (!silent && recipient != null) {
			recipient.sendMessage(messages);
		}
	}

	public void setOp(boolean value) {
		originator.setOp(value);
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}
}
