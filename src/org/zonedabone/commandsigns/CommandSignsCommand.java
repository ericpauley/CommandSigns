package org.zonedabone.commandsigns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandSignsCommand implements CommandExecutor {
    
    private CommandSigns plugin;
    
    public CommandSignsCommand(CommandSigns plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("commandsigns")) {
            if (args.length < 1) {
                return false;
            }
            Player tp = null;
            if (sender instanceof Player) {
                tp = (Player) sender;
            }
            final Player player = tp;
            Pattern pattern = Pattern.compile("(line|l|)(\\d+)");
            Matcher matcher = pattern.matcher(args[0]);
            if (matcher.matches()) {
                if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.create.regular")) {
                    int lineNumber = Integer.parseInt(matcher.group(2));
                    if (lineNumber <= 0) {
                        Messaging.sendMessage(player, "failure.invalid_line");
                        return true;
                    }
                    if (plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT_SELECT) {
                        Messaging.sendMessage(player, "failure.must_select");
                        return true;
                    }
                    CommandSignsText text = plugin.playerText.get(player);
                    if (text == null) {
                        text = new CommandSignsText(player.getName(), false);
                        plugin.playerText.put(player, text);
                    }
                    String line = StringUtils.join(args, " ", 1, args.length);
                    if (line.startsWith("/*") && !plugin.hasPermission(player, "commandsigns.create.super", false)) {
                        Messaging.sendMessage(player, "failure.no_super");
                        return true;
                    }
                    if ((line.startsWith("/^") || line.startsWith("/#")) && !plugin.hasPermission(player, "commandsigns.create.op", false)) {
                        Messaging.sendMessage(player, "failure.no_op");
                        return true;
                    }
                    text.setLine(lineNumber, line);
                    text.trim();
                    String display = line.replace("$", "\\$");
                    Messaging.sendRaw(player, "success.line_print", "n", "" + lineNumber, "l", display);
                    if (plugin.playerStates.get(player) != CommandSignsPlayerState.EDIT) {
                        plugin.playerStates.put(player, CommandSignsPlayerState.ENABLE);
                        Messaging.sendMessage(player, "progress.add");
                    }
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("read")) {
                if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.create.regular")) {
                    if (plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT || plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT_SELECT) {
                        finishEditing(player);
                    }
                    plugin.playerStates.put(player, CommandSignsPlayerState.READ);
                    Messaging.sendMessage(player, "progress.read");
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("view")) {
                if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.create.regular")) {
                    CommandSignsText text = plugin.playerText.get(player);
                    if (text == null) {
                        player.sendMessage("No text in clipboard");
                        return true;
                    }
                    int i = 0;
                    for (String s : text.getText()) {
                        if (!s.equals("")) {
                            player.sendMessage(i + ": " + s);
                        }
                        i++;
                    }
                    plugin.playerStates.remove(player);
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("copy")) {
                if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.create.regular")) {
                    if (plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT || plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT_SELECT) {
                        finishEditing(player);
                    }
                    plugin.playerStates.put(player, CommandSignsPlayerState.COPY);
                    Messaging.sendMessage(player, "progress.copy");
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.remove")) {
                    if (plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT || plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT_SELECT) {
                        finishEditing(player);
                    }
                    plugin.playerStates.put(player, CommandSignsPlayerState.REMOVE);
                    Messaging.sendMessage(player, "progress.remove");
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("clear")) {
                if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.remove")) {
                    if (plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT || plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT_SELECT) {
                        finishEditing(player);
                    }
                    plugin.playerStates.remove(player);
                    plugin.playerText.remove(player);
                    Messaging.sendMessage(player, "success.cleared");
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("save")) {
                if (plugin.hasPermission(sender, "commandsigns.save", false)) {
                    plugin.saveFile();
                    Messaging.sendMessage(sender, "success.saved");
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (plugin.hasPermission(sender, "commandsigns.reload", false)) {
                    Messaging.loadMessages(plugin);
                    plugin.loadFile();
                    plugin.startMetrics();
                    plugin.setupPermissions();
                    plugin.setupEconomy();
                    Messaging.sendMessage(sender, "success.reloaded");
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (plugin.hasPermission(sender, "commandsigns.edit", false)) {
                    CommandSignsPlayerState cs = plugin.playerStates.get(player);
                    if (cs == CommandSignsPlayerState.EDIT_SELECT || cs == CommandSignsPlayerState.EDIT) {
                        finishEditing(player);
                    } else {
                        plugin.playerStates.put(player, CommandSignsPlayerState.EDIT_SELECT);
                        plugin.playerText.remove(player);
                        Messaging.sendMessage(player, "progress.select_sign");
                    }
                }
            } else if (args[0].equalsIgnoreCase("toggle")) {
            	if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.toggle")) {
                    if (plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT || plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT_SELECT) {
                        finishEditing(player);
                    }
                    plugin.playerStates.put(player, CommandSignsPlayerState.TOGGLE);
                    Messaging.sendMessage(player, "progress.toggle");
                } else {
                	Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("redstone")) {
                if (player == null) {
                    Messaging.sendMessage(sender, "failure.player_only");
                    return true;
                }
                if (plugin.hasPermission(player, "commandsigns.create.redstone")) {
                	if (plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT || plugin.playerStates.get(player) == CommandSignsPlayerState.EDIT_SELECT) {
                        finishEditing(player);
                    }
                    plugin.playerStates.put(player, CommandSignsPlayerState.REDSTONE);
                    Messaging.sendMessage(player, "progress.redstone");
                } else {
                	 Messaging.sendMessage(player, "failure.no_perms");
                }
            } else if (args[0].equalsIgnoreCase("batch")) {
                CommandSignsPlayerState cs = plugin.playerStates.get(player);
                switch (cs) {
                    case REMOVE:
                        player.sendMessage("Switched to batch remove mode.");
                        cs = CommandSignsPlayerState.BATCH_REMOVE;
                        break;
                    case BATCH_REMOVE:
                        player.sendMessage("Switched to single remove mode.");
                        cs = CommandSignsPlayerState.REMOVE;
                        break;
                    case ENABLE:
                        player.sendMessage("Switched to batch enable mode.");
                        cs = CommandSignsPlayerState.BATCH_ENABLE;
                        break;
                    case BATCH_ENABLE:
                        player.sendMessage("Switched to single enable mode.");
                        cs = CommandSignsPlayerState.ENABLE;
                        break;
                    case READ:
                        player.sendMessage("Switched to batch read mode.");
                        cs = CommandSignsPlayerState.BATCH_READ;
                        break;
                    case BATCH_READ:
                        player.sendMessage("Switched to single read mode.");
                        cs = CommandSignsPlayerState.READ;
                        break;
                    case TOGGLE:
                        player.sendMessage("Switched to batch toggle mode.");
                        cs = CommandSignsPlayerState.BATCH_TOGGLE;
                        break;
                    case BATCH_TOGGLE:
                        player.sendMessage("Switched to single toggle mode.");
                        cs = CommandSignsPlayerState.TOGGLE;
                        break;
                    case REDSTONE:
                        player.sendMessage("Switched to batch redstone mode.");
                        cs = CommandSignsPlayerState.BATCH_REDSTONE;
                        break;
                    case BATCH_REDSTONE:
                        player.sendMessage("Switched to single redstone mode.");
                        cs = CommandSignsPlayerState.REDSTONE;
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "The mode you are in doesn't support batch processing.");
                }
                plugin.playerStates.put(player, cs);
            } else if (args[0].equalsIgnoreCase("update")) {
                if (sender.hasPermission("commandsigns.update")) {
                    if (args.length == 2 && args[1].equalsIgnoreCase("force")) {
                    	// Force-only. Does no check.
                        Messaging.sendMessage(sender, "update.force");
                        plugin.updateHandler.new Updater(sender).start();
                    } else {
                    	// Preliminary check
                        Messaging.sendMessage(sender, "update.check");
                        new Thread() {
                            
                            @Override
                            public void run() {
                                plugin.updateHandler.new Checker().run();
                                if (plugin.updateHandler.newAvailable) {
                                    Messaging.sendMessage(sender, "update.notify", "v", plugin.updateHandler.newestVersion.toString());
                                } else {
                                    Messaging.sendMessage(sender, "update.confirm_up_to_date", "v", plugin.updateHandler.currentVersion.toString());
                                }
                            }
                        }.start();
                        
                        // If command was 'update check', stop here. Enough has been done.
                        if (!(args.length == 2 && args[1].equalsIgnoreCase("check"))) {
		                    if (plugin.updateHandler.newAvailable) {
		                        if (!plugin.getUpdateFile().exists()) {
		                            Messaging.sendMessage(sender, "update.start", "v", plugin.updateHandler.newestVersion.toString());
		                            plugin.updateHandler.new Updater(sender).start();
		                        } else {
		                            Messaging.sendMessage(sender, "update.already_downloaded");
		                        }
		                    }
                        }
	                }
                }
	        } else {
	            Messaging.sendMessage(sender, "failure.wrong_syntax");
	        }
	        return true;
	    }
	    return false;
    }
    
    public void finishEditing(Player player) {
        plugin.playerStates.remove(player);
        plugin.playerText.remove(player);
        Messaging.sendMessage(player, "success.done_editing");
    }
}
