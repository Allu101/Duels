package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.allu.duels.utils.FileHandler;
import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.CountDownTimer;
import com.allu.minigameapi.CountDownTimerListener;
import com.allu.minigameapi.MessageHandler;
import com.allu.minigameapi.ranking.SimpleRanking;

public class DuelsGame implements CountDownTimerListener {

	public enum GameType {
		RANKED, FRIEND_CHALLENGE
	}
	
	
	private enum GameState {
		FREE, STARTING, PLAYING, GAME_FINISH
	}
	

	private GameState currentGameState = GameState.FREE;
	private String arenaType;
	private GameType gameType;

	private List<Location> buildedBlocks = new ArrayList<Location>();
	private List<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private Location arenaCenterLoc, spawn1, spawn2;

	private Lobby lobby;
	private MessageHandler messages;
	private CountDownTimer timer;
	
	private int arenaXWidth = 50;
	private int arenaZWidth = 80;
	
	private SimpleRanking winsRanking;
	private SimpleRanking eloRanking;

	public DuelsGame(Lobby lobby, Location arenaCenterLoc, String arenaType, MessageHandler messages, SimpleRanking winsRanking, SimpleRanking eloRanking) {
		this.lobby = lobby;
		this.arenaCenterLoc = arenaCenterLoc;
		
		if (arenaType.equals("sumo")) {
			this.spawn1 = arenaCenterLoc.clone().add(0, 0, -3);
			this.spawn2 = arenaCenterLoc.clone().add(0, 0, 3);
		}
		else if (arenaType.equals("gridpvp")) {
			this.spawn1 = arenaCenterLoc.clone().add(0, 0, -8);
			this.spawn2 = arenaCenterLoc.clone().add(0, 0, 8);
		}
		else {
			this.spawn1 = arenaCenterLoc.clone().add(0, 0, -26);
			this.spawn2 = arenaCenterLoc.clone().add(0, 0, 26);
		}

		spawn2.setYaw(-180);
		this.arenaType = arenaType;
		this.timer = new CountDownTimer(this);
		this.messages = messages;
		this.winsRanking = winsRanking;
		this.eloRanking = eloRanking;
	}

	@Override
	public void onCountDownChange(int time) {

	}

	@Override
	public void onCountDownFinish() {
		Bukkit.getScheduler().runTask(Duels.plugin, new Runnable() {

			@Override
			public void run() {
				if (currentGameState == GameState.STARTING) {
					currentGameState = GameState.PLAYING;
					for (DuelsPlayer dp : players) {
						Player p = dp.getPlayer();
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
						p.setHealth(p.getMaxHealth());
						p.sendMessage(ChatColor.GREEN + "Duels alkaa!");
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
								"title " + p.getName() + " times 0 20 10");
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
								"title " + p.getName() + " title {\"text\":\"Duels alkaa!\",\"bold\":true,\"color\":\"blue\"}");
					}
				} else if (currentGameState == GameState.GAME_FINISH) {
					
					for (DuelsPlayer dp : players) {
						lobby.sendPlayerToLobby(dp);
					}
					
					timer.clearPlayers();
					players.clear();
					currentGameState = GameState.FREE;
				}
			}
		});
	}
	
	public void onPlayerDie(Player deadPlayer) {	
		deadPlayer.setGameMode(GameMode.SPECTATOR);
		deadPlayer.getWorld().strikeLightningEffect(deadPlayer.getLocation());
		lobby.clearPlayerInventoryAndEquipment(deadPlayer);
		
		for (DuelsPlayer dp : players) {
			if (!dp.getPlayer().equals(deadPlayer)) {
				gameEnd(dp);
				return;
			}
		}
	}

	public void gameEnd(DuelsPlayer winner) {	
		currentGameState = GameState.GAME_FINISH;
		
		for (DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			lobby.clearPotionEffect(p);
			
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			p.sendMessage("");
			p.sendMessage(
					messages.getCenteredMessage(ChatColor.GREEN + "Voittaja: " + ChatColor.GOLD + winner.getPlayer().getName()));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			
			if (dp.equals(winner)) {
				dp.addWin();
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " times 0 80 20");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " title {\"text\":\"VOITTO\",\"bold\":true,\"color\":\"green\"}");
			} else {
				dp.addLose();
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " times 0 80 20");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " title {\"text\":\"TAPPIO\",\"bold\":true,\"color\":\"red\"}");
			}
			
			if (gameType.equals(GameType.FRIEND_CHALLENGE)) {
				p.sendMessage(ChatColor.GRAY + "Kaveripelit eiv�t vaikuta ranking-pisteisiin");
			}
			else {
				
				DuelsPlayer opponent = getOtherPlayer(dp);
				
				if (dp != null && opponent != null) {
					
					double result = 0;
					if (dp.equals(winner))
						result = 1;
					
					double expectedScore = getExpectedScore(dp.getEloScore(), opponent.getEloScore());
					double eloChange = (result - expectedScore) * 32 + 0.2; // + 0.2 for little total score increase over time.
					int finalEloChange = (int)Math.round(eloChange);
					
					dp.setEloScore(dp.getEloScore() + finalEloChange);
					
					if (finalEloChange == 0) {
						p.sendMessage(ChatColor.GRAY + "Ranking-pisteesi eiv�t muuttuneet pelin tuloksena.");
					}
					else if (finalEloChange < 0) {
						p.sendMessage(ChatColor.RED + "Menetit " + (-finalEloChange) + " ranking-pistett�");
					}
					else if (finalEloChange > 0) {
						p.sendMessage(ChatColor.GREEN + "Sait " + finalEloChange + " ranking-pistett�!");
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 0f);
					}
				}
			}

			
			Duels.plugin.dbHandler.saveStatsToDatabaseSQL(dp);
		}
		
		winsRanking.updateRankingWithPlayers(Duels.plugin.dbHandler.loadTop10PlayersToWinsScoreboard());
		eloRanking.updateRankingWithPlayers(Duels.plugin.dbHandler.loadTop10PlayersToEloScoreScoreboard());
		
		timer.start(5, "");
	}
	
	private double getExpectedScore(int eloOwn, int eloOther) {
		return 1.0 / (1.0 + Math.pow(10, (eloOther - eloOwn) / 400.0));
	}
	
	public String getArenaType() {
		return this.arenaType;
	}

	public List<Location> getPlacedBlocks() {
		return buildedBlocks;
	}

	public boolean isFree() {
		return currentGameState == GameState.FREE;
	}

	public boolean isGameOn() {
		return currentGameState == GameState.PLAYING;
	}
	
	public boolean isGameStarting() {
		return currentGameState == GameState.STARTING;
	}

	public void leaveGame(DuelsPlayer dp) {
		players.remove(dp);
		if(players.size() > 0) {
			gameEnd(players.get(0));
		}
	}

	public void startGame(List<DuelsPlayer> dplayers, Kit kit, GameType gameType) {
		
		currentGameState = GameState.STARTING;
		
		this.players = dplayers;
		this.gameType = gameType;
		
		// Remove all the arrows from the arena.
		Bukkit.getScheduler().runTaskLater(Duels.plugin, new Runnable() {
			@Override
			public void run() {
				List<Entity> entities = arenaCenterLoc.getWorld().getEntities();
				for (Entity entity : entities) {
					EntityType eType = entity.getType();
					if(eType.equals(EntityType.ARROW) && isWithinArena(entity.getLocation())) {
						entity.remove();
					}
				}
			}
		}, 10);
		
		teleportPlayersToSpawnPoints();
		
		for (DuelsPlayer dp : dplayers) {
			dp.setGameWhereJoined(this);
			
			Player p = dp.getPlayer();
			
			timer.addPlayer(p);
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
			lobby.clearPotionEffect(p);
			p.setScoreboard(dp.getSidebarHandler().getGameBoard());
			
			DuelsPlayer opponent = this.getOtherPlayer(dp);
			String opponentString = "";
			if (opponent != null) {
				opponentString = opponent.getPlayer().getName();
			}
			dp.getSidebarHandler().updateGameSidebar(getGameTypeString(this.gameType), kit.getName(), opponentString);
			
			p.getInventory().clear();
			p.updateInventory();
			
			Bukkit.getScheduler().runTaskLater(Duels.plugin, new Runnable() {
				@Override
				public void run() {
					setKitItems(p, kit.getItems());
				}
			}, 10);
		}
		
		timer.start(3, "Duelsin alkuun");
		
		FileHandler.increaseKitPlayedCount(kit.getName());
	}
	
	private void setKitItems(Player p, List<ItemStack> items) {
		
		for (ItemStack is : items) {
			String itemTypeString = is.getType().toString();
			if (itemTypeString.contains("HELMET")) {
				p.getInventory().setHelmet(new ItemStack(is));
			}
			else if (itemTypeString.contains("LEGGINGS")) {
				p.getInventory().setLeggings(new ItemStack(is));
			}
			else if (itemTypeString.contains("CHESTPLATE")) {
				p.getInventory().setChestplate(new ItemStack(is));
			}
			else if (itemTypeString.contains("_BOOTS")) {
				p.getInventory().setBoots(new ItemStack(is));
			}
			else {
				if (is.getMaxStackSize() == 1) {
					int count = is.getAmount();
					ItemStack oneItemIS = new ItemStack(is);
					oneItemIS.setAmount(1);
					for (int i = 0; i < count; i++) {
						p.getInventory().addItem(new ItemStack(oneItemIS));
					}
				}
				else {
					p.getInventory().addItem(new ItemStack(is));
				}
			}
		}
		
		p.updateInventory();
	}
	

	private void teleportPlayersToSpawnPoints() {
		for (int i = 0; i < players.size(); i++) {
			if (i % 2 == 0) {
				players.get(i).getPlayer().teleport(spawn1, TeleportCause.PLUGIN);
			} else {
				players.get(i).getPlayer().teleport(spawn2, TeleportCause.PLUGIN);
			}
		}
	}
	
	private DuelsPlayer getOtherPlayer(DuelsPlayer dpp) {
		for (DuelsPlayer dpp2: this.players) {
			if (!dpp.equals(dpp2)) {
				return dpp2;
			}
		}
		return null;
	}
	
	public boolean isWithinArena(Location loc) {
		if (!loc.getWorld().equals(this.arenaCenterLoc.getWorld())) {
			return false;
		}
		long halfArenaXWidht = this.arenaXWidth / 2;
		long halfArenaZWidht = this.arenaZWidth / 2;
		if (loc.getBlockX() < this.arenaCenterLoc.getBlockX() - halfArenaXWidht
				|| loc.getBlockX() > this.arenaCenterLoc.getBlockX() + halfArenaXWidht
				|| loc.getBlockZ() < this.arenaCenterLoc.getBlockZ() - halfArenaZWidht
				|| loc.getBlockZ() > this.arenaCenterLoc.getBlockZ() + halfArenaZWidht) {
			return false;
		}
		return true;
	}
	
	public boolean isNearArenaFloorLevel(double y) {
		return Math.abs(y - this.arenaCenterLoc.getY()) < 2.5;
	}
	
	public boolean isUnderArena(Location loc) {
		if (!loc.getWorld().equals(this.arenaCenterLoc.getWorld())) {
			return false;
		}
		return loc.getBlockY() < this.arenaCenterLoc.getBlockY() - 4;
	}
	
	private String getGameTypeString(GameType gameType) {
		if (gameType.equals(GameType.FRIEND_CHALLENGE)) return "Kaverihaaste";
		if (gameType.equals(GameType.RANKED)) return "Kilpailullinen";
		return "???";
	}
}
