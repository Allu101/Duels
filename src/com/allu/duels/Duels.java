package com.allu.duels;

import com.allu.duels.utils.DatabaseHandler;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.ItemHelper;
import com.allu.minigameapi.ranking.SimpleRanking;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Duels extends JavaPlugin implements CommandExecutor {
	
	public static Duels plugin;
	public static int matchMinutesUntilDraw = 15;
	
    public FileConfiguration config;
    private File cFile;
    
	public DatabaseHandler dbHandler = new DatabaseHandler(this);
	
	private static String LOBBY_WORLD;
	private ArrayList<Kit> kits = new ArrayList<>();
	
	private Events events;
	private ItemHelper itemHelper = new ItemHelper();
	private Lobby lobby;
	private MenuHandler menuHandler;	
	
	public SimpleRanking winsRanking;
	public SimpleRanking eloRanking;
	
	@Override
    public void onEnable() {
		
		plugin = this;
		
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
        cFile = new File(getDataFolder(), "config.yml");
	    
	    winsRanking = new SimpleRanking(dbHandler.loadPlayersToWinsScoreboard());
	    eloRanking = new SimpleRanking(dbHandler.loadPlayersToEloScoreScoreboard());
	    
		LOBBY_WORLD = config.getString("lobbyworldname");
	    createWorldIfDoesntExist(LOBBY_WORLD);
		menuHandler = new MenuHandler(this, itemHelper);
		lobby = new Lobby(config, menuHandler);
		events = new Events(lobby, menuHandler);
		
		getServer().getPluginManager().registerEvents(events, this);
		this.getCommand("duel").setExecutor(events);
		this.getCommand("spectate").setExecutor(events);
		this.getCommand("lobby").setExecutor(events);
		
		this.getCommand("duelsreload").setExecutor(this);
		this.getCommand("kits").setExecutor(this);
		
	    ConfigurationSection arenaSection = config.getConfigurationSection("arenas");
	    for (String key : arenaSection.getKeys(false)) {
	    	createGames(key);
	    	System.out.println("Created gameworld for " + key);
	    }
	    
	    applyConfig();
		
		
		World world = Bukkit.getWorld(LOBBY_WORLD);
		winsRanking.addFloatingRankingList(new Location(world, 5.5, 12.5, -37.5),
				"" + ChatColor.BLUE + ChatColor.BOLD + "- Voitot -", ChatColor.BLUE, ChatColor.GREEN);	
		eloRanking.addFloatingRankingList(new Location(world, 27.5, 12.5, -15.5),
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
	
	private void applyConfig() {
		this.kits.clear();
		this.loadKitsFromConfig();
		Duels.matchMinutesUntilDraw = config.getInt("match-minutes-until-draw", 15);
	}
	
	private void createGames(String arenaName) {
		String path = "arenas." + arenaName;
		int available_games = config.getInt(path + ".gamesavailable");
		System.out.println(arenaName + " games available: " + available_games);
		
		String gameWorld = "gameworld_" + arenaName;
	    createWorldIfDoesntExist(gameWorld);
	    
		for (int i = 0; i < available_games; i++) {
			Arena arena = new Arena(arenaName, getArenaCenterLoc(i+1, arenaName, gameWorld), config.getInt(path + ".spawn-distance", 1));
			lobby.addGame(new DuelsGame(lobby, arena, this.winsRanking, this.eloRanking));
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
	
	private Location getArenaCenterLoc(int orderNumber, String arenaType, String world) {
		String path = "arenas." + arenaType + ".firstarenacenter.";
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
			
			if (config.isConfigurationSection(kitPath + ".items")) {
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
							potion.setLevel(config.getInt(itemPath + ".level", 1));
							
							// This line causes error: "Instant potions cannot be extended"
							//potion.setHasExtendedDuration(config.getBoolean(itemPath + ".extended", false));
							
							potion.apply(is);
						}
					}
					
					kitItems.add(is);
				}
			}
			
			String kitName = config.getString(kitPath + ".name") + " Duel";
			ItemStack kitMenuItem = itemHelper.createItemWithTitle(Material.getMaterial(config.getString(kitPath + ".menuitem")),
					"§9" + kitName,
					getKitMenuItemBodyText(kitItems));
			
			String arenaType = config.getString(kitPath + ".arena", "default");
			Boolean invulnerable = config.getBoolean(kitPath + ".invulnerable", false);
			
			Kit kit = new Kit(kitMenuItem, kitItems, kitName, arenaType, invulnerable);
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
		
		lines.add("§r ");
		lines.add("§fKitin sisältä:");
		
		for (ItemStack item : items) {
			
			int amount = item.getAmount();
			
			lines.add("§7" + amount + "x " + getItemNameString(item));
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
			config = YamlConfiguration.loadConfiguration(cFile);
			applyConfig();
			sender.sendMessage("§aConfiguration reloaded successfully");
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







