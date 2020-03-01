package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.allu.duels.utils.DatabaseHandler;
import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.ItemHelpper;
import com.allu.minigameapi.MessageHandler;
import com.allu.minigameapi.ranking.SimpleRanking;

public class Duels extends JavaPlugin {
	
	public static Duels plugin;
	
	public DatabaseHandler dbHandler = new DatabaseHandler(this);
	
	
	
	private static String LOBBY_WORLD;
	private FileConfiguration config = this.getConfig();
	private ArrayList<Kit> kits = new ArrayList<>();
	
	private Events events;
	private ItemHelpper itemHelpper = new ItemHelpper();
	private Lobby lobby;
	private MenuHandler menuHandler;
	
	private SimpleRanking winsRanking;
	

	
	
	
	@Override
    public void onEnable() {
		plugin = this;
		this.saveDefaultConfig();
		config.options().copyDefaults(true);
	    saveConfig();
	    
	    winsRanking = new SimpleRanking(dbHandler.loadTop10PlayersToWinsScoreboard());
	    
		LOBBY_WORLD = config.getString("lobbyworldname");
	    createWorldIfDoesntExist(LOBBY_WORLD);
		menuHandler = new MenuHandler(this, itemHelpper);
		lobby = new Lobby(config, menuHandler);
		events = new Events(lobby, menuHandler);
		
		getServer().getPluginManager().registerEvents(events, this);
		this.getCommand("duel").setExecutor(events);
		this.getCommand("spectate").setExecutor(events);
		
	    loadKitsFromConfig();
		createGames(Gamemode.DUELS_1V1);
//		createGames(Gamemode.DUELS_2V2);
//		createGames(Gamemode.DUELS_4V4);
		
		World world = Bukkit.getWorld(LOBBY_WORLD);
		winsRanking.addFloatingRankingList(new Location(world, 5.5, 12, -37.5),
				"" + ChatColor.BLUE + ChatColor.BOLD + "- Voitot -", ChatColor.BLUE, ChatColor.GREEN);	
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
			lobby.addGame(new DuelsGame(lobby, getArenaCenterLoc(i+1, gameWorld), gameMode, new MessageHandler(), this.winsRanking));
		}
	}
	
	private void createWorldIfDoesntExist(String worldName) {
    	List<World> worlds = Bukkit.getWorlds();
    	for (World w : worlds) {
    		if (w.getName() == worldName) {
    			return;
    		}
    	}
    	WorldCreator wc = new WorldCreator(worldName);
    	Bukkit.createWorld(wc);
    }
	
	private Location getArenaCenterLoc(int orderNumber, String world) {
		String path = "firstarenacenter.";
		double x = config.getDouble(path + "x") * orderNumber;
		double y = config.getDouble(path + "y");
		double z = config.getDouble(path + "z");
		int yaw = config.getInt(path + "yaw");
		return new Location(Bukkit.getWorld(world), x + 0.5, y, z + 0.5, yaw, 0);
	}
	
	private void loadKitsFromConfig() {
		
		for (String key : config.getConfigurationSection("kits").getKeys(false)) {
			
			String kitPath = "kits." + key;
			
			List<ItemStack> kitItems = new ArrayList<>();
			for (String item : config.getConfigurationSection(kitPath + ".items").getKeys(false)) {
				kitItems.add(new ItemStack(Material.getMaterial(item), config.getInt(kitPath + ".items." + item)));
			}
			ItemStack kitMenuItem = itemHelpper.createItemWithTitle(Material.getMaterial(config.getString(kitPath + ".menuitem")), key + " Duel"
					, ChatColor.YELLOW + "Klikkaa liittyäksesi.");
			Kit kit = new Kit(kitMenuItem, kitItems, key);
			
			kits.add(kit);
		}
	}
}
