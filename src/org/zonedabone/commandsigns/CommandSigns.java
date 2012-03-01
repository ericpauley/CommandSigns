package org.zonedabone.commandsigns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandSigns extends JavaPlugin
{
	public final Logger logger = Logger.getLogger("Minecraft");
	
	//plugin variables
	public final HashMap<String, CommandSignsPlayerState> playerStates = new HashMap<String, CommandSignsPlayerState>();
	public final HashMap<CommandSignsLocation, CommandSignsText> activeSigns = new HashMap<CommandSignsLocation, CommandSignsText>();
	public final HashMap<String, CommandSignsText> playerText = new HashMap<String, CommandSignsText>();
	
	//listeners
	private final CommandSignsEventListener listener = new CommandSignsEventListener(this);
	private CommandSignsCommand commandExecutor = new CommandSignsCommand(this);
	
	@Override
	public void onDisable() 
	{
		saveFile();
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " is disabled.");
	}

	@Override
	public void onEnable() 
	{
		loadFile();
		PluginManager pm = getServer().getPluginManager();
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " version" + pdfFile.getVersion() + " is enabled.");
		
		getCommand("commandsigns").setExecutor(commandExecutor);

		pm.registerEvents(listener, this);
	}
	
	public boolean hasPermission(Player player, String string) {
		boolean permission = player.hasPermission(string);
		if(permission == false) {
			player.sendMessage("§cYou do not have permission.");
		}
		return permission;
	}
	
	public void loadFile() {
		File folder = getDataFolder();
		String folderName = folder.getParent();
		try {
			File file = new File(folderName + "/CommandSigns/signs.dat");
			if(file.exists()) {
				FileInputStream inStream = new FileInputStream(file);
				Scanner scanner = new Scanner(inStream);
				String line;
				String[] data;
				String[] locationData;
				String[] textData;
				while(scanner.hasNextLine()) {
					try {
						line = scanner.nextLine();
						if(!line.equals("")) {
							data = line.split(":", 2);
							locationData = data[0].split(",");
							CommandSignsLocation location = new CommandSignsLocation(Integer.parseInt(locationData[0]), 
																					Integer.parseInt(locationData[1]), 
																					Integer.parseInt(locationData[2]));
							textData = data[1].split("\\[LINEBREAK]");
							CommandSignsText text = new CommandSignsText();
							for(int i = 0; i < textData.length; i++) {
								text.setLine(i, textData[i]);
							}
							this.activeSigns.put(location, text);
						}
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
				scanner.close();
				inStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void saveFile() {
		File folder = getDataFolder();
		String folderName = folder.getParent();
		try {
			File file = new File(folderName + "/CommandSigns/signs.dat");
			if(!file.exists()) {
				folder.mkdir();
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("");
			Iterator<CommandSignsLocation> iterator = this.activeSigns.keySet().iterator();
			while(iterator.hasNext()) {
				CommandSignsLocation key = (CommandSignsLocation)iterator.next();
				String keyString = key.toFileString();
				String value = this.activeSigns.get(key).toFileString();
				String line = (keyString + ":" + value + "\n");
				writer.write(line);
			}
			writer.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}