package org.zonedabone.commandsigns;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;


public class CommandSignsProxy implements ConsoleCommandSender {

	private ConsoleCommandSender original;
	private CommandSender receiver;
	
	public CommandSignsProxy(ConsoleCommandSender original, CommandSender receiver){
		this.original = original;
		this.receiver = receiver;
	}

	public boolean isOp() {
		return original.isOp();
	}

	public boolean isConversing() {
		return original.isConversing();
	}

	public boolean isPermissionSet(String name) {
		return original.isPermissionSet(name);
	}

	public void setOp(boolean value) {
		original.setOp(value);
	}

	public void acceptConversationInput(String input) {
		original.acceptConversationInput(input);
	}

	public Server getServer() {
		return original.getServer();
	}

	public boolean isPermissionSet(Permission perm) {
		return original.isPermissionSet(perm);
	}

	public String getName() {
		return original.getName();
	}

	public boolean beginConversation(Conversation conversation) {
		return original.beginConversation(conversation);
	}

	public boolean hasPermission(String name) {
		return original.hasPermission(name);
	}

	public void abandonConversation(Conversation conversation) {
		original.abandonConversation(conversation);
	}

	public boolean hasPermission(Permission perm) {
		return original.hasPermission(perm);
	}

	public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
		original.abandonConversation(conversation, details);
	}

	public void sendRawMessage(String message) {
		original.sendRawMessage(message);
	}

	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		return original.addAttachment(plugin, name, value);
	}

	public PermissionAttachment addAttachment(Plugin plugin) {
		return original.addAttachment(plugin);
	}

	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
		return original.addAttachment(plugin, name, value, ticks);
	}

	public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		return original.addAttachment(plugin, ticks);
	}

	public void removeAttachment(PermissionAttachment attachment) {
		original.removeAttachment(attachment);
	}

	public void recalculatePermissions() {
		original.recalculatePermissions();
	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return original.getEffectivePermissions();
	}
	
	public void sendMessage(String message) {
		receiver.sendMessage(message);
	}

	public void sendMessage(String[] messages) {
		receiver.sendMessage(messages);
	}
	
}
