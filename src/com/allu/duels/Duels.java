package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;

public class Duels extends JavaPlugin implements Listener{
	
	public static Duels plugin;
	
	private static String LOBBY_WORLD;
	private FileConfiguration config = this.getConfig();
	private ArrayList<Kit> kits = new ArrayList<>();
	
	private Events events;
	private MenuHandler menuHandler;
	private Lobby lobby;
	
	@Override
    public void onEnable() {
		plugin = this;
		this.saveDefaultConfig();
		config.options().copyDefaults(true);
	    saveConfig();
	    
	    loadKitsFromConfig();
		LOBBY_WORLD = config.getString("lobbyworldname");
	    createWorldIfDoesntExist(LOBBY_WORLD);
		menuHandler = new MenuHandler(this);
		lobby = new Lobby(config, menuHandler);
		events = new Events(lobby, menuHandler);
		
		getServer().getPluginManager().registerEvents(events, this);
		this.getCommand("duel").setExecutor(events);
		
		createGames(Gamemode.DUELS_1V1);
		createGames(Gamemode.DUELS_2V2);
//		createGames(Gamemode.DUELS_4V4);
	}
	
	@Override
    public void onDisable() {
		
	}
	
	public ArrayList<Kit> getKits() {
		return kits;
	}
	
	public static String getLobbyWorldName() {
		return LOBBY_WORLD;
	}
	
	private void createGames(Gamemode gameMode) {
		String path = "duels" + gameMode.getString();
		int available_games = config.getInt(path + ".gamesavailable");
		for (int i = 0; i < available_games; i++) {
			String gameWorld = "gameworld_" + gameMode.getString();
		    createWorldIfDoesntExist(gameWorld);
			lobby.addGame(new DuelsGame(lobby, getArenaCenterLoc(i+1, gameWorld), gameMode));
		}
	}
	
	private void createWorldIfDoesntExist(String worldName) {
    	List<World> worlds = Bukkit.getWorlds();
    	for(World w : worlds) {
    		System.out.println(w.getName());
    		if (w.getName() == worldName) {
    			w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    			w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    			return;
    		}
    	}
    	WorldCreator wc = new WorldCreator(worldName);
    	Bukkit.createWorld(wc);
    }
	
	private Location getArenaCenterLoc(int orderNumber, String world) {
		String path = "firstarenacenter.";
		int x = config.getInt(path + "x") * orderNumber;
		int y = config.getInt(path + "y");
		int z = config.getInt(path + "z");
		int yaw = config.getInt(path + "yaw");
		return new Location(Bukkit.getWorld(world), x + 0.5, y, z + 0.5, yaw, 0);
	}
	
	private void loadKitsFromConfig() {
		List<ItemStack> kitItems = new ArrayList<>();
		for(String key : config.getConfigurationSection("kits").getKeys(false)) {
			String kitPath = "kits." + key;
			for(String item : config.getConfigurationSection(kitPath + ".items").getKeys(false)) {
				kitItems.add(new ItemStack(Material.getMaterial(item), config.getInt(kitPath + "." + item)));
			}
			Kit kit = new Kit(new ItemStack(Material.getMaterial(config.getString(kitPath + ".menuitem"))), kitItems);
			kits.add(kit);
			kitItems.clear();
		}
	}
}
