package com.allu.duels;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.allu.duels.utils.DatabaseHandler;
import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.ItemHelpper;
import com.allu.minigameapi.MessageHandler;
import com.allu.minigameapi.ranking.SimpleRanking;

public class Duels extends JavaPlugin implements CommandExecutor {
	
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
	private SimpleRanking eloRanking;	
	
	@Override
    public void onEnable() {
		plugin = this;
		this.saveDefaultConfig();
	    
	    winsRanking = new SimpleRanking(dbHandler.loadTop10PlayersToWinsScoreboard());
	    eloRanking = new SimpleRanking(dbHandler.loadTop10PlayersToEloScoreScoreboard());
	    
		LOBBY_WORLD = config.getString("lobbyworldname");
	    createWorldIfDoesntExist(LOBBY_WORLD);
		menuHandler = new MenuHandler(this, itemHelpper);
		lobby = new Lobby(config, menuHandler);
		events = new Events(lobby, menuHandler);
		
		getServer().getPluginManager().registerEvents(events, this);
		this.getCommand("duel").setExecutor(events);
		this.getCommand("spectate").setExecutor(events);
		this.getCommand("lobby").setExecutor(events);
		
		this.getCommand("duelsreload").setExecutor(this);
		this.getCommand("kits").setExecutor(this);
		
	    loadKitsFromConfig();
		createGames(Gamemode.DUELS_1V1);
//		createGames(Gamemode.DUELS_2V2);
//		createGames(Gamemode.DUELS_4V4);
		
		World world = Bukkit.getWorld(LOBBY_WORLD);
		winsRanking.addFloatingRankingList(new Location(world, 5.5, 11, -37.5),
				"" + ChatColor.BLUE + ChatColor.BOLD + "- Voitot -", ChatColor.BLUE, ChatColor.GREEN);	
		eloRanking.addFloatingRankingList(new Location(world, 27.5, 11, -15.5),
				"" + ChatColor.BLUE + ChatColor.BOLD + "- Ranking-pisteet -", ChatColor.BLUE, ChatColor.GREEN);	
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
		System.out.println(gameMode.toString() + " games available: " + available_games);
		for (int i = 0; i < available_games; i++) {
			String gameWorld = "gameworld_" + gameMode.getString();
		    createWorldIfDoesntExist(gameWorld);
			lobby.addGame(new DuelsGame(lobby, getArenaCenterLoc(i+1, gameWorld), gameMode, new MessageHandler(), this.winsRanking, this.eloRanking));
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
				String itemPath = kitPath + ".items." + item;	
				
				
				Material mat = Material.STONE;
				if (Material.getMaterial(item) != null) {
					mat = Material.getMaterial(item);
				} else {
					if (item.startsWith("potion")) {
						mat = Material.POTION;
					} else {
						System.out.println("Error loading: " + itemPath);
					}
				}
				
				ItemStack is = new ItemStack(mat, config.getInt(itemPath, 1));
				
				if (config.isConfigurationSection(itemPath)) {			
					
					is.setAmount(config.getInt(itemPath + ".amount", 1));
					
					if (Material.getMaterial(item) != null) {
						mat = Material.getMaterial(item);
					} else {
						if (item.startsWith("potion")) {
							mat = Material.POTION;
						}
					}
					
						
					String enchantmentsPath = itemPath + ".enchantments";
					if (config.isConfigurationSection(enchantmentsPath)) {
						for (String enchantment : config.getConfigurationSection(enchantmentsPath).getKeys(false)) {
							is.addUnsafeEnchantment(Enchantment.getByName(enchantment), config.getInt(enchantmentsPath + "." + enchantment));
						}
					}
					
					String potionTypePath = itemPath + ".potionEffect";
					if (config.isSet(potionTypePath)) {
						
						Potion potion = new Potion(PotionType.getByEffect(PotionEffectType.getByName(config.getString(potionTypePath))));
						if (config.getBoolean(itemPath + ".splash", false)) {
							potion.setSplash(true);
						}
						
						potion.apply(is);
					}
				}
				
				kitItems.add(is);
			}
			
			String kitName = config.getString(kitPath + ".name") + " Duel";
			ItemStack kitMenuItem = itemHelpper.createItemWithTitle(Material.getMaterial(config.getString(kitPath + ".menuitem")),
					"�9" + kitName,
					getKitMenuItemBodyText(kitItems));
			Kit kit = new Kit(kitMenuItem, kitItems, kitName);
			kits.add(kit);
		}
	}
	
	public Kit getKitByName(String kitName) {
		for (Kit kit : this.kits) {
			if (kit.getName().equalsIgnoreCase(kitName)) {
				return kit;
			}
		}
		return kits.get(0);
	}
	
	private String[] getKitMenuItemBodyText(List<ItemStack> items) {
		
		List<String> lines = new ArrayList<String>();
		
		lines.add("�r ");
		lines.add("�fKitin sis�lt�:");
		
		for (ItemStack item : items) {
			
			int amount = item.getAmount();
			
			lines.add("�7" + amount + "x " + getItemNameString(item));
		}
		
		return lines.toArray(new String[lines.size()]);
	}
	
	private String getItemNameString(ItemStack item) {
		String name = item.getType().toString().toLowerCase().replace('_', ' ');
		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		return name;
	}
	
	public Lobby getLobby() {
		return this.lobby;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		if(cmd.getName().equalsIgnoreCase("duelsreload")) {
			this.reloadConfig();
			this.kits.clear();
			this.loadKitsFromConfig();
			sender.sendMessage("�aKits have been reloaded!");
			sender.sendMessage("There are " + this.kits.size() + " kits.");
		}
		if(cmd.getName().equalsIgnoreCase("kits")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Komento vain pelaajille!");
				return true;
			}
			DuelsPlayer dp = lobby.getDuelsPlayer((Player)sender);
			if (dp != null) {
				dp.setChallengedPlayer(null);
				((Player)sender).openInventory(this.menuHandler.createKitMenu());	
			}
		}
		return true;
	}
}







